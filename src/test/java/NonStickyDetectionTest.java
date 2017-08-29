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

import com.splunk.cloudfwd.Connection;
import com.splunk.cloudfwd.Event;
import com.splunk.cloudfwd.IllegalHECStateException;
import com.splunk.cloudfwd.util.PropertiesFileHelper;
import com.splunk.cloudfwd.EventBatch;
import com.splunk.cloudfwd.RawEvent;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ghendrey
 */
public class NonStickyDetectionTest extends AbstractConnectionTest {

  public NonStickyDetectionTest() {
  }

  @After
  @Override
  public void tearDown() {
    //must use closeNow, because close() waits for channels to empty. But do to the failure that we are
    //*trying* to induce with this test, the channels will never empty
    this.connection.closeNow();
  }

  @Override
  protected BasicCallbacks getCallbacks() {
    return new BasicCallbacks(getNumEventsToSend()) {
      @Override
      public void failed(EventBatch events, Exception e) {
        //The point of this test is to insure that we DO get this exception...
        //because it means we DID *detect* a non-sticky channel and fail
        //appropriately.
        Assert.
                assertTrue(e.getMessage(),
                        e instanceof IllegalHECStateException);
        System.out.println("Got expected exception: " + e);
        latch.countDown(); //allow the test to finish
      }

      @Override
      public void checkpoint(EventBatch events) {
        System.out.println("SUCCESS CHECKPOINT " + events.getId());
        //do NOT count down the latch - otherwise the test ends before we have
        //opportunity to detect the non-sticky session
      }

    };
  }

  @Test
  public void checkNonStickyChannelDetected() throws InterruptedException {

    try {
      super.eventType = EventType.TEXT;
      super.sendEvents();
    } catch (TimeoutException e) {
      System.out.println(
              "Got expected timeout exception because all channels are broken (per test design): " + e.
              getMessage());
    }
  }

  @Override
  protected Properties getProps() {
    Properties props = new Properties();
    //props.put(PropertiesFileHelper.MOCK_HTTP_KEY, "true");
    //simulate a non-sticky endpoint
    props.put(PropertiesFileHelper.MOCK_HTTP_CLASSNAME_KEY,
            "com.splunk.cloudfwd.sim.errorgen.nonsticky.NonStickEndpoints");
    return props;
  }
  
  @Override
  protected void configureConnection(Connection connection) {
    connection.setCharBufferSize(0);
  }  

  @Override
  protected int getNumEventsToSend() {
    return 1000;
  }
  /*
   public static void main(String[] args) {
    new NonStickyDetectionTest().runTests();    
  }
   */

}
