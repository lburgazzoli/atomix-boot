/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lburgazzoli.atomix.boot.client;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.resource.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("atomix.client")
public class AtomixBootClientConfiguration {
    /**
     * Enable the replica auto configuration.
     */
    private boolean enabled = true;

    /**
     * The bootstrap cluster configuration
     */
    @NotEmpty
    private List<Address> nodes = new ArrayList<>();

    /**
     * The client transport type.
     */
    private Class<? extends Transport> transportType;

    /**
     * The resource types.
     */
    private List<Class<? extends Resource<?>>> resourceTypes = new ArrayList<>();

    /**
     * The replica election timeout
     */
    private Long electionTimeout;

    /**
     * The replica's global suspend timeout
     */
    private Long globalSuspendTimeout;

    /**
     * The replica heartbeat interval.
     */
    private Long heartbeatInterval;

    /**
     * The replica session timeout;
     */
    private Long sessionTimeout;

    // ****************************************
    // Properties
    // ****************************************

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Address> getNodes() {
        return nodes;
    }

    public void setNodes(List<Address> nodes) {
        this.nodes = nodes;
    }

    public Class<? extends Transport> getTransportType() {
        return transportType;
    }

    public void setTransportType(Class<? extends Transport> transportType) {
        this.transportType = transportType;
    }

    public List<Class<? extends Resource<?>>> getResourceTypes() {
        return resourceTypes;
    }

    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
