package com.splunk.cloudfwd.test.mock.hec_server_error_response_tests;

import com.splunk.cloudfwd.LifecycleEvent;
import com.splunk.cloudfwd.error.HecConnectionTimeoutException;
import com.splunk.cloudfwd.error.HecServerErrorResponseException;
import com.splunk.cloudfwd.test.util.BasicCallbacks;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static com.splunk.cloudfwd.PropertyKeys.ACK_TIMEOUT_MS;
import static com.splunk.cloudfwd.PropertyKeys.BLOCKING_TIMEOUT_MS;
import static com.splunk.cloudfwd.PropertyKeys.MOCK_HTTP_CLASSNAME;

/**
 * Created by mhora on 10/3/17.
 */

public class HecServerErrorResponseAcksDisabledTest extends AbstractHecServerErrorResponseTest {
    private static final Logger LOG = LoggerFactory.getLogger(HecServerErrorResponseAcksDisabledTest.class.getName());

    protected int getNumEventsToSend() {
        return 3;
    }

    @Override
    protected BasicCallbacks getCallbacks() {
        return new BasicCallbacks(getNumEventsToSend()) {
            @Override
            public boolean shouldFail() {
                return true;
            }

            @Override
            protected boolean isExpectedFailureType(Exception e) {
                boolean isExpectedType = e instanceof HecServerErrorResponseException
                        && ((HecServerErrorResponseException) e).getCode() == 14;
                return isExpectedType;

            }
        };
    }

    @Override
    protected Properties getProps() {
        Properties props =  new Properties();
        props.put(MOCK_HTTP_CLASSNAME,
                "com.splunk.cloudfwd.impl.sim.errorgen.splunkcheckfailure.AckDisabledEndpoints");
        props.put(ACK_TIMEOUT_MS, "500000");  //in this case we expect to see HecConnectionTimeoutException
        props.put(BLOCKING_TIMEOUT_MS, "5000");
        return props;
    }

    @Override
    protected boolean isExpectedSendException(Exception e) {
        boolean isExpected = false;
        if (e instanceof HecConnectionTimeoutException) {
            isExpected = true;
        }
        return isExpected;
    }

    @Override
    protected boolean shouldSendThrowException() {
        return true;
    }

    //pre-flight check NOT ok
    @Test
    public void sendWithAcksDisabled() throws InterruptedException, TimeoutException, HecConnectionTimeoutException {
        LOG.info("TESTING ACKS_DISABLED");
        createConnection(LifecycleEvent.Type.ACK_DISABLED);
    }
}
