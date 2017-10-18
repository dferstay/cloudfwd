package mock_tests;

import com.splunk.cloudfwd.Connection;
import com.splunk.cloudfwd.ConnectionSettings;
import com.splunk.cloudfwd.Event;
import com.splunk.cloudfwd.error.HecConnectionTimeoutException;
import com.splunk.cloudfwd.PropertyKeys;
import com.splunk.cloudfwd.impl.ConnectionImpl;
import com.splunk.cloudfwd.impl.sim.ValidatePropsEndpoint;
import com.splunk.cloudfwd.impl.util.PropertiesFileHelper;
import test_utils.AbstractConnectionTest;
import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by eprokop on 9/11/17.
 */
public class ConnectionMutabilityTest extends AbstractConnectionTest {
    private int numEvents = 1000000;
    private int start = 0;
    private int stop = -1;
    private long ackPollWait = 1000;

    @Test
    public void setMultipleProperties() throws Throwable {
        setPropsOnEndpoint();
        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.RAW_EVENTS_ENDPOINT);
        super.eventType = Event.Type.TEXT;
        sendSomeEvents(getNumEventsToSend()/4);

        // Set some new properties
        ConnectionSettings settings = connection.getSettings();
        settings.setToken("a token");
        settings.setAckTimeoutMS(120000);
        settings.setUrls("https://127.0.0.1:8188");
        setPropsOnEndpoint();
        sendSomeEvents(getNumEventsToSend()/4);


        // Set the same properties
        settings.setToken("a token");
        settings.setAckTimeoutMS(120000);
        settings.setUrls("https://127.0.0.1:8188");
        setPropsOnEndpoint();
        sendSomeEvents(getNumEventsToSend()/4);


        // Set some more new properties
        settings.setToken("different token");
        settings.setAckTimeoutMS(240000);
        settings.setUrls("https://127.0.0.1:8288, https://127.0.0.1:8388");
        setPropsOnEndpoint();
        sendSomeEvents(getNumEventsToSend()/4);
        close();
        checkAsserts();
    }

    @Test
    public void changeEndpointType() throws Throwable {
        setPropsOnEndpoint();
        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.RAW_EVENTS_ENDPOINT);
        super.eventType = Event.Type.TEXT;
        sendSomeEvents(getNumEventsToSend()/4);
        

        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.STRUCTURED_EVENTS_ENDPOINT);
        super.eventType = Event.Type.TEXT;
        sendSomeEvents(getNumEventsToSend()/4);
        

        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.RAW_EVENTS_ENDPOINT);
        super.eventType = Event.Type.UNKNOWN;
        sendSomeEvents(getNumEventsToSend()/4);
        

        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.STRUCTURED_EVENTS_ENDPOINT);
        super.eventType = Event.Type.UNKNOWN;
        sendSomeEvents(getNumEventsToSend()/4);

        close();
        checkAsserts();
    }

    @Test
    public void changeToken() throws Throwable {
        setPropsOnEndpoint();
        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.RAW_EVENTS_ENDPOINT);
        super.eventType = Event.Type.TEXT;
        sendSomeEvents(getNumEventsToSend()/2);


        connection.getSettings().setToken("different token");
        setPropsOnEndpoint();
        sendSomeEvents(getNumEventsToSend()/2);
        close();
        checkAsserts();
    }

    @Test
    public void changeUrlsAndAckTimeout() throws Throwable {
        connection.getSettings().setHecEndpointType(Connection.HecEndpoint.RAW_EVENTS_ENDPOINT);
        super.eventType = Event.Type.TEXT;
        setPropsOnEndpoint();
        sendSomeEvents(getNumEventsToSend()/4);

        setUrls("https://127.0.0.1:8188");
        setAckTimeout(120000);

        sendSomeEvents(getNumEventsToSend()/4);

        setAckTimeout(65000);
        setUrls("https://127.0.0.1:8288, https://127.0.0.1:8388");

        sendSomeEvents(getNumEventsToSend()/4);

        setUrls("https://127.0.0.1:8488, https://127.0.0.1:8588, https://127.0.0.1:8688");
        setAckTimeout(80000);

        sendSomeEvents(getNumEventsToSend()/4);
        close();
        checkAsserts();
    }

    @Override
    protected void setProps(PropertiesFileHelper settings) {
        settings.setAckTimeoutMS(1000000);
        settings.setUnresponsiveMS(-1); //no dead channel detection
        settings.setMockHttp(true);
        // the asserts for this test exist in the endpoint since we must check values server side
        settings.setMockHttpClassname("com.splunk.cloudfwd.impl.sim.ValidatePropsEndpoint");
    }

    private void setPropsOnEndpoint() {
        ValidatePropsEndpoint.URLS = connection.getSettings().getUrls();
        ValidatePropsEndpoint.ACK_TIMEOUT_MS = connection.getSettings().getAckTimeoutMS();
        ValidatePropsEndpoint.TOKEN = connection.getSettings().getToken();
    }

    private void checkAsserts() throws Throwable {
        Throwable e;
        if ((e = ValidatePropsEndpoint.getAssertionFailures()) != null) {
            throw e;
        }
    }

    protected int getNumEventsToSend() {
        return numEvents;
    }

    private void setUrls(String urls) throws UnknownHostException {
        connection.getSettings().setUrls(urls);
        ValidatePropsEndpoint.URLS = connection.getSettings().getUrls();
    }

    private void setAckTimeout(long ms) {
        connection.getSettings().setAckTimeoutMS(ms);
        ValidatePropsEndpoint.ACK_TIMEOUT_MS = connection.getSettings().getAckTimeoutMS();
    }

    @Override
    protected void configureConnection(Connection connection) {
        connection.getSettings().setEventBatchSize(1024*32); //32k batching batching, roughly
    }

    private void sendSomeEvents(int numEvents) throws InterruptedException, HecConnectionTimeoutException {
        System.out.println(
                "SENDING EVENTS WITH CLASS GUID: " + AbstractConnectionTest.TEST_CLASS_INSTANCE_GUID
                        + "And test method GUID " + testMethodGUID);

        stop += numEvents;
        System.out.println("Start = "+start + " stop = " + stop);
        for (int i = start; i <= stop; i++) {
            Event event = nextEvent(i + 1);
            connection.send(event);
        }
        start = stop + 1;
        connection.flush();
        //this should really be done with a latch on acn ack counter in the acknowledged callback
        //but what the hell, test a different code path
        while(!((ConnectionImpl)connection).getUnackedEvents().isEmpty()){
            sleep(ackPollWait);
        }        
    }

    private void close() throws InterruptedException, HecConnectionTimeoutException {
        connection.close(); //will flush
        this.callbacks.await(10, TimeUnit.MINUTES);
        if (callbacks.isFailed()) {
            Assert.fail("There was a failure callback with exception class  "
                    + callbacks.getException() + " and message " + callbacks.getFailMsg());
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}