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
package com.splunk.cloudfwd.impl.sim.errorgen.indexer;

import com.splunk.cloudfwd.impl.http.httpascync.HttpCallbacksAbstract;
import com.splunk.cloudfwd.impl.http.HecIOManager;
import com.splunk.cloudfwd.impl.http.HttpPostable;
import com.splunk.cloudfwd.impl.sim.AckEndpoint;
import com.splunk.cloudfwd.impl.sim.AcknowledgementEndpoint;
import com.splunk.cloudfwd.impl.sim.HealthEndpoint;
import com.splunk.cloudfwd.impl.sim.EventEndpoint;
import com.splunk.cloudfwd.impl.sim.SimulatedHECEndpoints;
import static com.splunk.cloudfwd.impl.sim.errorgen.indexer.DownIndexerEndpoints.LOG;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import java.net.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate communicating with indexer in manual detention
 * @author meemax
 */
public class DownIndexerEndpoints extends SimulatedHECEndpoints {
   static final Logger LOG = LoggerFactory.getLogger(DownIndexerEndpoints.class.getName());
  @Override
  public void checkAckEndpoint(FutureCallback<HttpResponse> httpCallback) {
    LOG.trace("splunk ack endpoint check fails because down");
    ((HttpCallbacksAbstract)httpCallback).failed(new ConnectException("Unable to connect"));
  }

  @Override
  protected AcknowledgementEndpoint createAckEndpoint() {
    return new DownIndexerAckEndpoint();
  }

  @Override
  protected EventEndpoint createEventEndpoint() {
    return new DownIndexerEventEndpoint();
  }

  @Override
  protected HealthEndpoint createHealthEndpoint() {
    return new DownIndexerHealthEndpoint();
  }
}

class DownIndexerAckEndpoint extends AckEndpoint {
  @Override
  public void pollAcks(HecIOManager ackMgr, FutureCallback<HttpResponse> cb) {
    LOG.trace("splunk ack endpoint check fails because down");
    ((HttpCallbacksAbstract) cb).failed(new Exception("Unable to connect"));
  }

  @Override
  public void start() {

  }
}

class DownIndexerHealthEndpoint extends HealthEndpoint {
  @Override
  public void pollHealth(FutureCallback<HttpResponse> cb) {
    LOG.trace("/health rest endpoint fails because down");
    ((HttpCallbacksAbstract) cb).failed(new Exception("Unable to connect"));
  }
}

class DownIndexerEventEndpoint extends EventEndpoint {
  @Override
  public void post(HttpPostable events, final FutureCallback<HttpResponse> cb) {
    LOG.trace("/event rest endpoint fails because down");
    Runnable respond = new Runnable() {
      @Override
      public void run() {
        ((HttpCallbacksAbstract) cb).failed(new Exception("Unable to connect"));
      }
    };
    delayResponse(respond);
  }
}