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
package com.splunk.cloudfwd;

/**
 *
 * @author ghendrey
 */
public class LifecycleEvent {

  public enum Type {
    EVENT_BATCH_BORN,
    PRE_EVENT_POST,
    EVENT_POST_NOT_OK,
    EVENT_POST_FAILURE,
    EVENT_POST_OK,
    EVENT_POST_ACKS_DISABLED,
    EVENT_TIMED_OUT,
    ACK_POLL_OK,
    ACK_POLL_NOT_OK,
    ACK_POLL_FAILURE,
    ACK_DISABLED,
    UNHANDLED_NON_200,
    
     //ELB state
     GATEWAY_TIMEOUT, //504 from ELB when it cuts off response due to timeout

    // States without an EventBatch object
    HEALTH_POLL_OK,
    INDEXER_BUSY,
    HEALTH_POLL_FAILED,
    HEALTH_POLL_ERROR,
    SPLUNK_IN_DETENTION,
    INVALID_TOKEN,

    // Needed to know statuses, do not throw exception
    PREFLIGHT_HEALTH_CHECK_PENDING,
    PREFLIGHT_GATEWAY_TIMEOUT,
    PREFLIGHT_BUSY,
    PREFLIGHT_OK,
    PREFLIGHT_NOT_OK,
    INVALID_AUTH

  };

  private final Type type;

  public LifecycleEvent(final Type type) {
    this.type = type;
  }

  /**
   * @return the type
   */
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "LifecycleEvent{" + "type=" + type + '}';
  }
  
    /**
     *return Exception associated with this LifecycleEvent, or null if this LifecycleEvent is not associated with an Exception
     * @return
     */
    public Exception getException(){
        return null;
    }
  
  


}