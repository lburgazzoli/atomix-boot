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
package com.github.lburgazzoli.atomix.boot.node.discovery;

import io.atomix.cluster.discovery.NodeDiscoveryConfig;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;

/**
 * Discovery Config
 */
public class AtomixNodeDiscoveryProviderConfig extends NodeDiscoveryConfig {
    private String namespace;
    private String portName;
    private String portProtocol;
    private String zone;
    private String domain;
    private String serviceName;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getPortProtocol() {
        return portProtocol;
    }

    public void setPortProtocol(String portProtocol) {
        this.portProtocol = portProtocol;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public NodeDiscoveryProvider.Type getType() {
        return AtomixNodeDiscoveryProvider.TYPE;
    }

    @Override
    public String toString() {
        return "AtomixNodeDiscoveryProviderConfig{" +
            "namespace='" + namespace + '\'' +
            ", portName='" + portName + '\'' +
            ", portProtocol='" + portProtocol + '\'' +
            ", zone='" + zone + '\'' +
            ", domain='" + domain + '\'' +
            ", serviceName='" + serviceName + '\'' +
            '}';
    }
}
