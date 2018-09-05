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
package com.github.lburgazzoli.atomix.boot.node.autoconfigure;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableSet;
import io.atomix.cluster.BootstrapService;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.NodeDiscoveryConfig;
import io.atomix.cluster.discovery.NodeDiscoveryEvent;
import io.atomix.cluster.discovery.NodeDiscoveryEventListener;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.utils.event.AbstractListenerManager;
import io.atomix.utils.net.Address;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointPort;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.AutoAdaptableKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AtomixNodeDiscoveryProvider
    extends AbstractListenerManager<NodeDiscoveryEvent, NodeDiscoveryEventListener>
    implements NodeDiscoveryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixNodeDiscoveryProvider.class);
    private static final Type TYPE = new Type();

    private final Config config;
    private final Map<Address, Node> nodes;
    private final NamespacedKubernetesClient client;
    private final AtomicBoolean watching;

    private Watch watch;
    private BootstrapService bootstrap;

    public AtomixNodeDiscoveryProvider(Config config) {
        this.config = config;
        this.nodes = new ConcurrentHashMap<>();
        this.watching = new AtomicBoolean();
        this.client = new AutoAdaptableKubernetesClient();
    }

    @Override
    public Set<Node> getNodes() {
        if (watching.compareAndSet(false, true)) {
            // subscribe to events
            watch = client.endpoints().inNamespace(config.namnespace).withName(config.endpointName).watch(new Watcher<Endpoints>() {
                @Override
                public void eventReceived(Action action, Endpoints resource) {
                    handle(action, resource);
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                    handle(Action.ERROR, null);
                }
            });
        }

        handle(
            Watcher.Action.MODIFIED,
            client.endpoints().inNamespace(config.namnespace).withName(config.endpointName).get()
        );

        return ImmutableSet.copyOf(nodes.values());
    }

    @Override
    public CompletableFuture<Void> join(BootstrapService bootstrap, Node localNode) {
        if (nodes.putIfAbsent(localNode.address(), localNode) == null) {
            this.bootstrap = bootstrap;
            post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.JOIN, localNode));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> leave(Node localNode) {
        if (nodes.remove(localNode.address()) != null) {
            post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.LEAVE, localNode));
            LOGGER.info("Left");

            if (watching.compareAndSet(true, false)) {
                if (watch != null) {
                    watch.close();
                    watch = null;
                }
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public NodeDiscoveryConfig config() {
        return config;
    }

    /**
     * Discovery Type
     */
    public static class Type implements NodeDiscoveryProvider.Type<Config> {
        private static final String NAME = "kubernetes";

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Config newConfig() {
            return new Config();
        }

        @Override
        public NodeDiscoveryProvider newProvider(Config config) {
            return new AtomixNodeDiscoveryProvider(config);
        }
    }

    /**
     * Discovery Config
     */
    public static class Config extends NodeDiscoveryConfig {
        private String namnespace;
        private String endpointName;
        private String portName;

        public String getNamnespace() {
            return namnespace;
        }

        public void setNamnespace(String namnespace) {
            this.namnespace = namnespace;
        }

        public String getEndpointName() {
            return endpointName;
        }

        public void setEndpointName(String endpointName) {
            this.endpointName = endpointName;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        @Override
        public NodeDiscoveryProvider.Type getType() {
            return TYPE;
        }
    }

    // ************************
    //
    // ************************

    private void handle(Watcher.Action action, Endpoints resource) {
        if (action == Watcher.Action.ADDED || action == Watcher.Action.MODIFIED) {
            for (EndpointSubset subset : resource.getSubsets()) {

                if (subset.getPorts().size() == 1) {
                    addNode(subset.getPorts().get(0), subset);
                } else {
                    final List<EndpointPort> ports = subset.getPorts();
                    final int portSize = ports.size();

                    EndpointPort port;
                    for (int p = 0; p < portSize; p++) {
                        port = ports.get(p);

                        // todo: check port name
                        addNode(port, subset);
                    }
                }
            }
        } else {
            for (Node node : nodes.values()) {
                post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.LEAVE, node));
            }

            nodes.clear();
        }
    }

    private void addNode(EndpointPort port, EndpointSubset subset) {
        final List<EndpointAddress> addresses = subset.getAddresses();
        final int size = addresses.size();

        for (int i = 0; i < size; i++) {
            EndpointAddress ea = addresses.get(0);
            Address nodeAddress = Address.from(ea.getIp(), port.getPort());
            Node node = Node.builder().withAddress(nodeAddress).withId(ea.getHostname()).build();

            if (nodes.putIfAbsent(nodeAddress, node) == null) {
                post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.JOIN, node));
            }
        }
    }


}
