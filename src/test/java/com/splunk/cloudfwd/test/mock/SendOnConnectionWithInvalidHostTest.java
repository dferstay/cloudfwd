package com.splunk.cloudfwd.test.mock;

import com.splunk.cloudfwd.HecHealth;
import com.splunk.cloudfwd.PropertyKeys;
import com.splunk.cloudfwd.error.HecConnectionTimeoutException;
import com.splunk.cloudfwd.error.HecMaxRetriesException;
import com.splunk.cloudfwd.error.HecNoValidChannelsException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static com.splunk.cloudfwd.PropertyKeys.MOCK_HTTP_CLASSNAME;
import static com.splunk.cloudfwd.PropertyKeys.MOCK_HTTP_KEY;

/**
 * Scenario: Invalid host URL
 * Expected behavior: Connection instantiation succeeds and 
 * send throws expected exception
 *
 * Created by eprokop on 10/5/17.
 */
public class SendOnConnectionWithInvalidHostTest extends ExceptionOnSendTest {

  @Override
  protected Properties getProps() {
      Properties props = new Properties();
      props.put(PropertyKeys.COLLECTOR_URI, "https://foobarunknownhostbaz:8088");
      props.put(MOCK_HTTP_CLASSNAME, "com.splunk.cloudfwd.impl.sim.errorgen.discoverer.InvalidHostName");
      return props;
  }
  
  @Test
  public void sendThrowsAndHealthContainsExpectedException() throws InterruptedException, HecConnectionTimeoutException {
    super.sendEvents(false, false);
    assertAllChannelsFailed(HecMaxRetriesException.class, 
            "URI does not specify a valid host name: foobarunknownhostbaz:8088/services/collector/ack");
  }
  
  @Override
  protected boolean shouldSendThrowException() {return true;}
  
}
