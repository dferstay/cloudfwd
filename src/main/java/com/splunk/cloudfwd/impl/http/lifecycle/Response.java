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
package com.splunk.cloudfwd.impl.http.lifecycle;

import com.splunk.cloudfwd.LifecycleEvent;
import com.splunk.cloudfwd.impl.http.ServerErrors;
import java.io.IOException;

/**
 *
 * @author ghendrey
 */
public class Response extends LifecycleEvent {

  private final int httpCode;
  private final String resp;
  private final String url;

  public Response(final Type type, int httpCode, String resp, String url) {
    super(type);
    this.httpCode = httpCode;
    this.resp = resp;
    this.url = url;
  }
  
  @Override
  public Exception getException(){
      if(isOK()){
          return null;// OK = no Exception
      }else{
          try {
              return ServerErrors.toErrorException(resp, httpCode, url);
          } catch (IOException ex) {
              throw new RuntimeException("ServerErrors.toErrorException failed. Http status="+ httpCode+", resp="+resp, ex);
          }
      }
  }

  @Override
    public boolean isOK() {
        return httpCode == 200;
    }

    @Override
    public String toString() {
        return "Response{" + super.toString() + " httpCode=" + httpCode + ", resp=" + resp + ", url=" + url + '}';
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

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

}

