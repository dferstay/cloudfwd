/*
 * Copyright 2017 Splunk, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.splunk.cloudfwd.impl.sim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splunk.cloudfwd.impl.http.HecIOManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author meemax
 */
public class ContinuousAckEndpoint extends ClosableDelayableResponder implements AcknowledgementEndpoint {
    
    private static final Logger LOG = LoggerFactory.getLogger(ContinuousAckEndpoint.class.
            getName());
    private static final ObjectMapper serializer = new ObjectMapper();
    
    protected AtomicLong ackId = new AtomicLong(-1); //so post increment, first id returned is 0
    protected Collection<Long> acked = new HashSet<>(); // toggled as acked
    protected Collection<Long> unacked = new TreeSet<>(); // not yet acked
    
    volatile boolean started;
    final ScheduledThreadPoolExecutor executor;
    private final Map resp = new HashMap(); //accessed from synchronized block
    SortedMap<Long, Boolean> acks = new TreeMap<>();    //accessed from synchronized block
    
    public ContinuousAckEndpoint() {
        ThreadFactory f = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "AckEndpoint");
            }
        };
    this.executor = new ScheduledThreadPoolExecutor(1, f);
    executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    executor.setRemoveOnCancelPolicy(true);
    }

    //start periodically flipping ackIds from false to true. This simulated event batches getting indexed.
    //To mimic the observed behavior of splunk, we flip the lowest unacknowledged ackId before
    //any higher ackId
    @Override
    public void start() {
        if (started) {
            return;
        }
        synchronized(this){
          if (started) {
             return;
         }    
          //stateFrobber will set the ack to TRUE
          Runnable stateFrobber = new Runnable() {
              @Override
              public void run() {
                  try {
                      synchronized (ContinuousAckEndpoint.this) {         
                          if(unacked.isEmpty()){
                             return;
                          }                 
                          int flipCount = (int) Math.ceil(Math.random()*1000); //change a random number of acks to true  [1,1000]
                          for(Iterator<Long> it = ((TreeSet<Long>)unacked).iterator(); it.hasNext();) {
                            Long l = it.next();
                            unacked.remove(l);
                            acked.add(l);
                            if(--flipCount==0){
                                return;
                            }
                          }
                          /*
                          Long lowestKey = acksStates.firstKey();
                          if (null == lowestKey) {
                              return;
                          }
                          acksStates.put(lowestKey, true); //flip it
                          */
                      }//synchronized
                  } catch (Exception e) {
                      LOG.error(e.getMessage(), e);
                  }
              }
          };
          //NOTE: with fixed *DELAY* NOT scheduleAtFixedRATE. The latter will cause threads to pile up
          //if the execution time of a task exceeds the period. We don't want that.
          executor.scheduleWithFixedDelay(stateFrobber, 0, 10,
                  TimeUnit.MILLISECONDS);
          started = true;
      }//end synchronized
    }
    
    @Override
    public long nextAckId() {
        long newId = this.ackId.incrementAndGet();
        synchronized(this) {
          this.unacked.add(newId);
        }
        //System.out.println("ackStates: " + this.acksStates);
        return newId;
    }
    
//    private synchronized Boolean check(long ackId) {
//        //System.out.println("checking " + ackId);
//        return this.acksStates.remove(ackId);
//    }
    
    @Override
    public synchronized void pollAcks(HecIOManager ackMgr,
            FutureCallback<HttpResponse> cb) {
        try {
            //System.out.println("Server side simulation: " + this.acksStates.size() + " acks tracked on server: " + acksStates);
            Collection<Long> unacked = ackMgr.getAcknowledgementTracker().
                    getPostedButUnackedEvents();
            //System.out.println("Channel  " +AckEndpoint.this.toString()+" received these acks to check: " + unacked + " and had this state " + acksStates);      
            acks.clear();
            for (long ackId : unacked) {
                boolean has = acked.contains(ackId);
                if (has) {
                    acked.remove(ackId);
                    acks.put(ackId, Boolean.TRUE);
                }
            }
            resp.clear();
            resp.put("acks", acks);
            final HttpResponse httpResp = getResult(resp); //this must be calculated and made final, not call getResult from Runnable since we are using class fields (acks, and resp) which can mutate during the delay before the runnable runs
            Runnable r = () -> {
                cb.completed(httpResp);
            };
            delayResponse(r);
            //executor.schedule(r, 1, TimeUnit.MILLISECONDS);
            //System.out.println("these are the ack states returned from the server: "+acks);
            
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            cb.failed(ex);
        }
    }
    
    protected HttpResponse getResult(Map acks) {
        String str = null;
        try {
            str = serializer.writeValueAsString(acks);
        } catch (JsonProcessingException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException(str, ex);
        }
        AckEndpointResponseEntity e = new AckEndpointResponseEntity(str);
        return new AckEndpointResponse(e);
    }
    
    @Override
    public void close() {
        super.close();
        LOG.debug("SHUTDOWN ACK FROBBER SIMULATOR");
        this.executor.shutdownNow(); 
        try {
            if (!executor.isTerminated() && !executor.awaitTermination(10,
                    TimeUnit.SECONDS)) {
                LOG.error("Failed to terminate executor in alloted time.");
            }            
        } catch (InterruptedException ex) {
            LOG.error(
                    "Interrupted awaiting termination of AckEndpoint executor.");
        }
    }
    
    private static class AckEndpointResponseEntity extends CannedEntity {
        
        public AckEndpointResponseEntity(String acks) {
            super(acks);
        }
    }
    
    private static class AckEndpointResponse extends CannedOKHttpResponse {
        
        public AckEndpointResponse(AckEndpointResponseEntity e) {
            super(e);
        }
    }
}
