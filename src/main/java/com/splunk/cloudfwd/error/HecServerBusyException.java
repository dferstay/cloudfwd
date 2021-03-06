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
package com.splunk.cloudfwd.error;

/**
 * Each time an EventBatch is resent due to busy server (504,504 response on POST), an HECServerBusyException is 
 * added to the EventBatch's list of Exceptions
 * @author ghendrey
 */
public class HecServerBusyException extends Exception{

    public HecServerBusyException(String message) {
        super(message);
    }
    
}
