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

import com.splunk.cloudfwd.LifecycleEvent;
import com.splunk.cloudfwd.impl.util.HecChannel;
import java.time.Duration;

/**
 *
 * @author ghendrey
 */
public interface HecHealth {
    
    public Duration getChannelAge();
    
    public String getChannelCreatorThreadName();
    
    /**
     * provides the time since the current value of isHealthy has been in its current state. For example, if isHealthy() is false, 
     * this method returns the Duration of time that the channel has been unhealthy, measured from the last moment the channel
     * that isHealthy() would have returned true.
     * @return
     */
    public Duration getTimeSinceHealthChanged();
    
    public LifecycleEvent getStatus();

    public String getUrl();

    public String getChannelId();
    
    public HecChannel getChannel();

    /**
     * @return the healthy
     */
    boolean isHealthy();

    /*
     * Return Exception responsible for LifecycleEvent returned by getStatus. This method has the same affect as
     * getStatus().getStatusException().
     * @return Exception that caused this state, or null if no Exception associated with this state.
     */
    public RuntimeException getStatusException();    

    public boolean isMisconfigured();

    public Exception getConfigurationException();
}
