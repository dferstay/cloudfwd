package com.splunk.cloudfwd.test.mock.health_check_tests;

import com.splunk.cloudfwd.ConnectionCallbacks;
import com.splunk.cloudfwd.Connections;
import com.splunk.cloudfwd.LifecycleEvent;
import com.splunk.cloudfwd.error.HecServerErrorResponseException;
import org.junit.Assert;
import com.splunk.cloudfwd.test.util.AbstractConnectionTest;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by mhora on 10/4/17.
 */
public class AbstractHealthCheckTest extends AbstractConnectionTest {
    private int numEvents = 10;

    protected int getNumEventsToSend() {
        return numEvents;
    }

    @Override
    public void setUp() {
        this.callbacks = getCallbacks();
        this.testMethodGUID = java.util.UUID.randomUUID().toString();
        this.events = new ArrayList<>();
    }

    // Need to separate this logic out of setUp() so that each Test
    // can use different simulated endpoints
    protected void createConnection(LifecycleEvent.Type problemType) {
        Properties props = new Properties();
        props.putAll(getTestProps());
        props.putAll(getProps());
        boolean gotException = false;
        try{
            this.connection = Connections.create((ConnectionCallbacks) callbacks, props);
        }catch(Exception e){
            Assert.assertTrue("Expected HecServerErrorResponseException but got "+ e,  e instanceof HecServerErrorResponseException);
            HecServerErrorResponseException servRespExc = (HecServerErrorResponseException) e;
            Assert.assertTrue("HecServerErrorResponseException not "+problemType+", was  " + servRespExc.getLifecycleType(),
                    servRespExc.getLifecycleType()==problemType);
            gotException = true;
        }
        if(!gotException){
            Assert.fail("Expected HecMaxRetriedException associated with Connection instantiation config checks'");
        }
        configureConnection(connection);
    }
}
