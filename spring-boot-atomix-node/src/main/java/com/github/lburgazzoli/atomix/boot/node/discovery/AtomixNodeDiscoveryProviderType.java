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

import io.atomix.cluster.discovery.NodeDiscoveryProvider;

/**
 * Discovery Type
 */
public class AtomixNodeDiscoveryProviderType implements NodeDiscoveryProvider.Type<AtomixNodeDiscoveryProviderConfig> {
    private static final String NAME = "k8s";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public AtomixNodeDiscoveryProviderConfig newConfig() {
        return new AtomixNodeDiscoveryProviderConfig();
    }

    @Override
    public NodeDiscoveryProvider newProvider(AtomixNodeDiscoveryProviderConfig config) {
        return new AtomixNodeDiscoveryProvider(config);
    }
}
