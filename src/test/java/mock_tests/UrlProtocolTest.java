package mock_tests;

import com.splunk.cloudfwd.ConnectionSettings;
import com.splunk.cloudfwd.PropertyKeys;
import com.splunk.cloudfwd.error.HecConnectionStateException;
import com.splunk.cloudfwd.impl.util.PropertiesFileHelper;

import static com.splunk.cloudfwd.error.HecConnectionStateException.Type.CONFIGURATION_EXCEPTION;

import java.net.UnknownHostException;
import java.util.Properties;

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


public class UrlProtocolTest extends ExceptionConnInstantiationTest {
     @Override
     protected void setProps(PropertiesFileHelper settings) {
         try {
             settings.setUrls("http://foo.com"); //http is not supported protocol. Must be https
         } catch (UnknownHostException e) {
             throw new RuntimeException(e);
         }
    }
    
    protected boolean isExpectedConnInstantiationException(Exception e) {
       if(e instanceof HecConnectionStateException){
           return ((HecConnectionStateException)e).getType() == CONFIGURATION_EXCEPTION
                   && ((HecConnectionStateException)e).getMessage().equals("protocol 'http' is not supported. Use 'https'.");
       }
       return false;
    }    
    
    
}