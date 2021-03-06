package com.splunk.cloudfwd.impl.sim;

import com.splunk.cloudfwd.impl.ConnectionImpl;
import com.splunk.cloudfwd.impl.http.HecIOManager;
import com.splunk.cloudfwd.impl.http.HttpPostable;
import com.splunk.cloudfwd.impl.http.HttpSender;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.junit.Assert;

import java.net.URL;
import java.util.List;
import com.splunk.cloudfwd.EventBatch;

/**
 * Validates Connection properties that are "live" and
 * take effect without delay.
 *
 * Created by eprokop on 9/11/17.
 */
public class ValidatePropsEndpoint extends SimulatedHECEndpoints {

    public static List<URL> URLS; // set with PropertiesFileHelper.getUrls()
    public static long ACK_TIMEOUT_MS;
    public static String TOKEN;
    private static Throwable fail = null;

    @Override
    public void pollAcks(HecIOManager ackMgr,
    FutureCallback<HttpResponse> httpCallback) {
        validate(ackMgr.getSender());
        ackEndpoint.pollAcks(ackMgr, httpCallback);
    }

    private void validate(HttpSender sender) {
        try {
            boolean match = false;
            for (URL url : URLS) {
                if (url.toString().equals(sender.getBaseUrl())) {
                    match = true;
                }
            }
            Assert.assertTrue("Sender url: " + sender.getBaseUrl()
                    + ", should match a url in url list: " + URLS.toString(), match);
            Assert.assertEquals("Ack timeouts should match.",
                    sender.getConnection().getPropertiesFileHelper().getAckTimeoutMS(), ACK_TIMEOUT_MS);
            Assert.assertEquals("Tokens should match.",
                    sender.getConnection().getPropertiesFileHelper().getToken(), TOKEN);
        } catch (AssertionError|Exception e) {
            fail = e;
            throw e; // so it shows up in console. still need to call getAssertionFailures
            // since this won't cause the test to fail as it isn't thrown in the main thread
        }
    }

    public static Throwable getAssertionFailures() {
        return fail;
    }
}
