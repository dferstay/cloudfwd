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

/**
 *
 * @author ghendrey
 */
public class HttpBodyAndStatus {
    private int statusCode;
    private String body;

    public HttpBodyAndStatus(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpBodyAndStatus{" + "statusCode=" + statusCode + ", body=" + body + '}';
    }
    
    

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }
    
}
