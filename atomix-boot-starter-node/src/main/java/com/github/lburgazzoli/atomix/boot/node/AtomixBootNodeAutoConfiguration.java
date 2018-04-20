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
package com.github.lburgazzoli.atomix.boot.node;

import java.util.Optional;

import io.atomix.cluster.Node;
import io.atomix.cluster.NodeId;
import io.atomix.core.Atomix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnClass(name = "io.atomix.core.Atomix")
@ConditionalOnProperty(value = "atomix.node.enabled", matchIfMissing = true)
@EnableDiscoveryClient(autoRegister=false)
@EnableConfigurationProperties(AtomixBootNodeConfiguration.class)
public class AtomixBootNodeAutoConfiguration {
    @Autowired
    private AtomixBootNodeConfiguration configuration;
    @Autowired(required = false)
    private DiscoveryClient discoveryClient;
    @Autowired(required = false)
    private ServiceRegistry serviceRegistry;

    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-node", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(AtomixBootNode.class)
    public AtomixBootNode atomixNode() {
        /*
        Atomix.builder(configuration.getCfg());

        Atomix.Builder builder = Atomix.builder();
        if (configuration.getStorage().getPath() != null) {
            builder.withDataDirectory(new File(configuration.getStorage().getPath()));
        }

        final AtomixBootNodeConfiguration.Cluster cluster = configuration.getCluster();
        final List<io.atomix.cluster.Node> nodes = new ArrayList();

        if (cluster.getName() != null) {
            builder.withClusterName(cluster.getName());
        }

        if (discoveryClient != null && cluster.getName() != null) {
            for (ServiceInstance instance: discoveryClient.getInstances(cluster.getName())) {
                String type = instance.getMetadata().getOrDefault("atomix.node.type", "CORE");
                String name = instance.getMetadata().get("atomix.node.name");

                Node.Builder nb = Node.builder();
                nb.withAddress(instance.getHost(), instance.getPort());
                nb.withType(Node.Type.valueOf(type));

                if (name != null) {
                    nb.withId(NodeId.from(name));
                }

                nodes.add(nb.build());
            }
        }

        for (AtomixBootNodeConfiguration.Node node : cluster.getNodes()) {
            nodes.add(asAtomixNode(node));
        }

        builder.withNodes(nodes);
        builder.withLocalNode(asAtomixNode(configuration.getNode()));
        */

        return new AtomixBootNode(
            Atomix.builder().build(),
            Optional.ofNullable(null),
            Optional.ofNullable(serviceRegistry)
        );
    }

    private Node asAtomixNode(AtomixBootNodeConfiguration.Node node) {
        Node.Builder nb = Node.builder();
        nb.withAddress(node.getAddress());
        nb.withType(node.getType());

        if (node.getName() != null) {
            nb.withId(NodeId.from(node.getName()));
        }

        return nb.build();
    }
}
