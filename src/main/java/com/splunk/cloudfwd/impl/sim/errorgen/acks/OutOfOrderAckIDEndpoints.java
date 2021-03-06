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
package com.splunk.cloudfwd.impl.sim.errorgen.acks;

import com.splunk.cloudfwd.impl.sim.AcknowledgementEndpoint;
import com.splunk.cloudfwd.impl.sim.RandomAckEndpoint;
import com.splunk.cloudfwd.impl.sim.SimulatedHECEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author meemax
 */
public class OutOfOrderAckIDEndpoints extends SimulatedHECEndpoints{
  private static final Logger LOG = LoggerFactory.getLogger(OutOfOrderAckIDEndpoints.class.getName());
  
  @Override
  protected AcknowledgementEndpoint createAckEndpoint() {
    return new RandomAckEndpoint();
  }
}
