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
package com.splunk.cloudfwd.impl.sim.errorgen.cookies;

import com.splunk.cloudfwd.impl.http.HecIOManager;
import com.splunk.cloudfwd.impl.http.HttpPostable;
import com.splunk.cloudfwd.impl.http.httpascync.HttpCallbacksAbstract;
import com.splunk.cloudfwd.impl.sim.*;

import com.splunk.cloudfwd.impl.sim.errorgen.PreFlightAckEndpoint;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateableCookieEndpoints extends SimulatedHECEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateableCookieEndpoints.class.getName());

    private static final String cookie1 = "tasty-cookie=strawberry";
    private static final String cookie2 = "bitter-cookie=crinkles";

    private static String currentCookie = cookie1;

    public static synchronized void toggleCookie() {
        if (currentCookie == cookie1)
            currentCookie = cookie2;
        else
            currentCookie = cookie1;
        LOG.info("Toggled cookie to " + currentCookie);
    }

    @Override
    public void checkAckEndpoint(FutureCallback<HttpResponse> httpCallback) {
        LOG.debug("Preflight check with cookie: " + currentCookie);
        httpCallback.completed(
                new CookiedOKHttpResponse(
                        new CannedEntity("{\\\"acks\\\":[0:false]}"),
                        currentCookie));
    }

    @Override
    public void checkHealthEndpoint(FutureCallback<HttpResponse> httpCallback) {
        LOG.debug("Health check with cookie: " + currentCookie);
        httpCallback.completed(
                new CookiedOKHttpResponse(
                        new CannedEntity("Healthy with cookies"),
                        currentCookie));
    }

    @Override
    protected PreFlightAckEndpoint createPreFlightAckEndpoint() {
        return new CookiedPreFlightEnpoint();
    }

    @Override
    protected EventEndpoint createEventEndpoint() {
        return new CookiedEventpoint();
    }

    @Override
    protected AckEndpoint createAckEndpoint() {
        return new CookiedAckEndpoint();
    }

    class CookiedPreFlightEnpoint extends PreFlightAckEndpoint {
        @Override
        public void checkAckEndpoint(final FutureCallback<HttpResponse> httpCallback) {
            Runnable respond = new Runnable() {
                @Override
                public void run() {
                    httpCallback.completed(
                      new CookiedOKHttpResponse(
                        new CannedEntity("{\\\"acks\\\":[0:false]}"),
                        currentCookie));
                }
            };
            delayResponse(respond);
        }
    }

    class CookiedEventpoint extends EventEndpoint {

        @Override
        public void post(HttpPostable events, final FutureCallback<HttpResponse> cb) {
            Runnable respond = new Runnable() {
                @Override
                public void run() {
                    LOG.debug("Event post response with cookie: " + currentCookie);
                    ((HttpCallbacksAbstract) cb).completed(
                      new CookiedOKHttpResponse(
                        new CannedEntity("{\"ackId\":" + nextAckId() + "}"),
                        currentCookie));
                }
            };
            delayResponse(respond);
        }

        protected long nextAckId() {
            return ackEndpoint.nextAckId();
        }
    }

    class CookiedAckEndpoint extends AckEndpoint {

        @Override
        public synchronized long nextAckId() {
            long newId = this.ackId.incrementAndGet();
            this.acksStates.put(newId, true);
            return newId;
        }

        @Override
        protected HttpResponse getHttpResponse(String entity) {
            LOG.info("Ack response with cookie: " + currentCookie);
            CannedEntity e = new CannedEntity(entity);
            return new CookiedOKHttpResponse(e, currentCookie);
        }
    }


}

