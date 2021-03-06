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
package com.splunk.cloudfwd.impl.http;

import java.io.Closeable;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

/**
 * This interface exists in order to allow us to make simulated HTTP endpoints.
 * @author ghendrey
 */
public interface Endpoints extends Closeable{
  public void postEvents(final HttpPostable events,FutureCallback<HttpResponse> httpCallback);
  public void pollAcks(HecIOManager ackMgr,FutureCallback<HttpResponse> httpCallback);
  public void checkHealthEndpoint(FutureCallback<HttpResponse> httpCallback);
  public void checkAckEndpoint(FutureCallback<HttpResponse> httpCallback);
  public void checkRawEndpoint(FutureCallback<HttpResponse> httpCallback);
  @Override
  public void close();
  public void start();
}
