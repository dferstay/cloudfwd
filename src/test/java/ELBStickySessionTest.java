import org.apache.http.concurrent.FutureCallback;
import com.splunk.cloudfwd.http.*;
import com.splunk.cloudfwd.sim.StickyEndpoints;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.HashMap;

/**
 * Created by eprokop on 8/14/17.
 */
public class ELBStickySessionTest {
    private HttpEventCollectorSender sender = new HttpEventCollectorSender("dummyUrl", "dummyToken");
    private StickyEndpoints simulatedStickyEndpoints = new StickyEndpoints();

    @Before
    public void setUp() {
        sender.setSimulatedEndpoints(simulatedStickyEndpoints);
    }

    @After
    public void tearDown() {
        // TODO
        //in case of failure we probably have events stuck on a channel. Therefore a regular close will just
        //hang out waiting (infinitely?) for the messages to flush out before gracefully closing. So when we see
        //a failure we must use the closeNow method which closes the channel regardless of whether it has
        //messages in flight.
//        if(ackTracker.isFailed()){
//            connection.closeNow();
//        }
    }

    private EventBatch nextEventBatch() {
        EventBatch events = new EventBatch(EventBatch.Endpoint.raw,
                EventBatch.Eventtype.json);
        events.add(new HttpEventCollectorEvent("info", "foo", "HEC_LOGGER",
                Thread.currentThread().getName(), new HashMap(), null, null));
        return events;
    }

    @Test
    public void testEventPostSetCookie() {
        EventBatch events = nextEventBatch();
        FutureCallback<HttpResponse> cb = new AbstractHttpCallback() {
            @Override
            protected void completed(String reply, int code, ElbCookie cookie) {
                if (code == 200) {
                    Assert.assertNotNull("Cookie value passed to consumeEventPostResponse should not be null",
                            cookie.getValue());
                    sender.getAckManager().consumeEventPostResponse(reply, events, cookie);
                    Assert.assertNotNull("Cookie in HttpEventCollectorSender should not be null", sender.getCookie());
                    Assert.assertNotNull("Cookie value in HttpEventCollectorSender should not be null", sender.getCookie().getValue());
                    Assert.assertEquals("Cookie name should be 'AWSELB'", sender.getCookie().getValue().split("=")[0], "AWSELB");
                    System.out.println("Done!");
                } else {
                    Assert.fail("Simulated endpoint broken: didn't receive a 200");
                }
            }

            @Override
            public void failed(Exception e) {

            }

            @Override
            public void cancelled() {

            }
        };

        sender.postEvents(events, null, cb);
    }

    private void runTests() {
        Result result = JUnitCore.runClasses(getClass());

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }

    public static void main(String[] args) {
        new ELBStickySessionTest().runTests();
    }

    // Tests:
    // 1. Send 5 eventBatches. First event does not include a cookie. Make sure the rest of the events have Cookie header and cookie is set in the sender
    // 2. Test different ways of sending cookie

}