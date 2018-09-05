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

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.google.common.collect.ImmutableSet;
import io.atomix.cluster.BootstrapService;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.NodeDiscoveryConfig;
import io.atomix.cluster.discovery.NodeDiscoveryEvent;
import io.atomix.cluster.discovery.NodeDiscoveryEventListener;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.utils.event.AbstractListenerManager;
import io.atomix.utils.net.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtomixNodeDiscoveryProvider
    extends AbstractListenerManager<NodeDiscoveryEvent, NodeDiscoveryEventListener>
    implements NodeDiscoveryProvider {

    public static final NodeDiscoveryProvider.Type TYPE;

    private static final Logger LOGGER;
    private static final String[] ATTRIBUTE_IDS;
    private static final Hashtable<String, String> ENV;

    static {
        TYPE = new AtomixNodeDiscoveryProviderType();

        LOGGER = LoggerFactory.getLogger(AtomixNodeDiscoveryProvider.class);
        ATTRIBUTE_IDS = new String[] {"SRV"};

        ENV = new Hashtable<>();
        ENV.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        ENV.put("java.naming.provider.url", "dns:");
    }

    private final AtomixNodeDiscoveryProviderConfig config;
    private final Map<Address, Node> nodes;
    private final AtomicBoolean watching;

    private ScheduledExecutorService scheduler;
    private BootstrapService bootstrap;

    public AtomixNodeDiscoveryProvider(AtomixNodeDiscoveryProviderConfig config) {
        this.config = config;
        this.nodes = new ConcurrentHashMap<>();
        this.watching = new AtomicBoolean();
    }

    @Override
    public Set<Node> getNodes() {
        LOGGER.info("============= getNodes ===============");

        if (watching.compareAndSet(false, true)) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            this.scheduler.scheduleAtFixedRate(this::watch, 0, 5, TimeUnit.SECONDS);
        }

        return ImmutableSet.copyOf(nodes.values());
    }

    @Override
    public CompletableFuture<Void> join(BootstrapService bootstrap, Node localNode) {
        LOGGER.info("============= join ===============");
        if (nodes.putIfAbsent(localNode.address(), localNode) == null) {
            this.bootstrap = bootstrap;
            post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.JOIN, localNode));

            if (watching.compareAndSet(false, true)) {
                this.scheduler = Executors.newSingleThreadScheduledExecutor();
                this.scheduler.scheduleAtFixedRate(this::watch, 0, 5, TimeUnit.SECONDS);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> leave(Node localNode) {
        LOGGER.info("============= leave ===============");
        if (nodes.remove(localNode.address()) != null) {
            post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.LEAVE, localNode));
            LOGGER.info("Left");

            if (watching.compareAndSet(true, false)) {
                this.scheduler.shutdownNow();
                this.scheduler = null;
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public NodeDiscoveryConfig config() {
        return config;
    }

    // ************************
    //
    // ************************

    private void watch() {
        String namespace = config.getNamespace() != null ? config.getNamespace() : System.getenv("KUBERNETES_NAMESPACE");
        String portName = config.getPortName();
        String portProtocol = config.getPortProtocol();
        String zone = config.getZone();
        String serviceName = config.getServiceName();

        // validation
        Objects.nonNull(namespace);
        Objects.nonNull(portName);
        Objects.nonNull(portProtocol);
        Objects.nonNull(zone);

        try {

            final String query = String.format("_%s._%s.%s.%s.svc.%s",
                portName,
                portProtocol,
                serviceName,
                namespace,
                zone
            );

            LOGGER.info("config={}, query={}", config, query);

            final DirContext ctx = new InitialDirContext(ENV);
            final NamingEnumeration<?> resolved = ctx.getAttributes(query, ATTRIBUTE_IDS).get("srv").getAll();

            if (resolved.hasMore()) {

                while (resolved.hasMore()) {
                    String record = (String)resolved.next();
                    String[] items = record.split(" ", -1);
                    String host = items[3].trim();
                    String port = items[2].trim();

                    Node node = Node.builder().withAddress(host, Integer.parseInt(port)).withId(host).build();

                    if (nodes.putIfAbsent(node.address(), node) == null) {
                        LOGGER.info("Node added: {}", node);
                        post(new NodeDiscoveryEvent(NodeDiscoveryEvent.Type.JOIN, node));
                    }
                }
            } else {
                LOGGER.warn("Could not find any service for name={}, query={}", serviceName, query);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not resolve services via DNSSRV", e);
        }
    }

}
