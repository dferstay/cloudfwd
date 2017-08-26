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
package com.splunk.cloudfwd.http.lifecycle;

/**
 *
 * @author ghendrey
 */
public class Response extends LifecycleEvent {

  private final int httpCode;
  private final String resp;

  public Response(final Type type, int httpCode, String resp) {
    super(type);
    this.httpCode = httpCode;
    this.resp = resp;
  }

  @Override
  public String toString() {
    return super.toString()  + " Response{" + "httpCode=" + httpCode + ", resp=" + resp + '}';
  }
  

  /**
   * @return the httpCode
   */
  public int getHttpCode() {
    return httpCode;
  }

  /**
   * @return the resp
   */
  public String getResp() {
    return resp;
  }
}
