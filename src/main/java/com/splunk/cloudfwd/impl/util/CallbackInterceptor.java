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
package com.splunk.cloudfwd.impl.util;

import com.splunk.cloudfwd.ConnectionCallbacks;
import com.splunk.cloudfwd.EventBatch;
import com.splunk.cloudfwd.impl.ConnectionImpl;
import com.splunk.cloudfwd.impl.EventBatchImpl;
import org.slf4j.Logger;

/**
 * Server EventTrackers keep track of EventBatches by their ids. When an
 EventBatchImpl fails, the EventTrackers must be canceled. When an EventBatchImpl is
 acknowledged, also the EventTrackers must be canceled, because in either case
 the EventBatchImpl is no longer tracked by the Connection.
 *
 * @author ghendrey
 */
public class CallbackInterceptor implements ConnectionCallbacks {
    private static Logger LOG;

    ConnectionCallbacks callbacks;
    CheckpointManager cpManager;

    public CallbackInterceptor(ConnectionCallbacks callbacks, ConnectionImpl c) {
        this.LOG = c.getLogger(CallbackInterceptor.class.getName());
        this.callbacks = callbacks;
        this.cpManager = c.getCheckpointManager();
    }

    @Override
    public void acknowledged(EventBatch events) {
        try {
            callbacks.acknowledged(events);
        } catch (Exception e) {
            LOG.error("Caught exception from ConnectionCallbacks.acknowledged: " + e.getMessage());
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                ((EventBatchImpl) events).cancelEventTrackers(); //remove the EventBatchImpl from the places in the system it should be removed
            } catch (Exception e) {
                LOG.error("Caught exception in finally block of callback interceptor acknowledged: " + e.getMessage());
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void failed(EventBatch events, Exception ex) {
        try {
            if(null != events && ((EventBatchImpl)events).isFailed()){
                LOG.debug("Ignoring failed call on already failed events {}", events);
                return;
            }
            this.callbacks.failed(events, ex);
        } catch (Exception e) {
            LOG.error("Caught exception from ConnectionCallbacks.failed: " + e.getMessage());
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (null != events) {
                    ((EventBatchImpl) events).setFailed(true);
                    ((EventBatchImpl) events).cancelEventTrackers();//remove the EventBatchImpl from the places in the system it should be removed
                    this.cpManager.release((EventBatchImpl) events);
                }
            } catch (Exception e) {
                LOG.error("Caught exception in finally block of callback interceptor failed: " + e.getMessage());
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void checkpoint(EventBatch events) {
        try {
            callbacks.checkpoint(events); //we don't need to wrap checkpoint at present
        } catch (Exception e) {
            LOG.error("Caught exception from ConnectionCallbacks.checkpoint: " + e.getMessage());
            LOG.error(e.getMessage(), e);
        }
    }

    public ConnectionCallbacks unwrap() {
        return this.callbacks;
    }

    @Override
    public void systemError(Exception e) {
        try {
            callbacks.systemError(e);
        } catch (Exception ex) {
            LOG.error("Caught exception from ConnectionCallbacks.systemError: " + ex.getMessage());
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void systemWarning(Exception e) {
        try {
            callbacks.systemWarning(e);
        } catch (Exception ex) {
            LOG.error("Caught exception from ConnectionCallbacks.systemWarning: " + ex.getMessage());
            LOG.error(ex.getMessage(), ex);
        }
    }

}
