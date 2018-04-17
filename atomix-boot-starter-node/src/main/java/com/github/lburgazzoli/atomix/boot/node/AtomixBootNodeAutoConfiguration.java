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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.lburgazzoli.atomix.boot.common.AtomixUtil;
import io.atomix.cluster.Node;
import io.atomix.cluster.NodeId;
import io.atomix.core.Atomix;
import io.atomix.messaging.Endpoint;
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
        Atomix.Builder builder = Atomix.builder();

        Node.Builder nodeBuilder = Node.builder().withType(Node.Type.DATA);
        if (configuration.getNodeId() != null) {
            nodeBuilder.withId(NodeId.from(configuration.getNodeId()));
        }
        if (configuration.getClusterName() != null) {
            builder.withClusterName(configuration.getClusterName());
        }

        Endpoint endpoint = AtomixUtil.asEndpoint(configuration.getEndpoint()).orElseThrow(() -> new IllegalStateException(""));

        if (configuration.getStorage().getPath() != null) {
            builder.withDataDirectory(new File(configuration.getStorage().getPath()));
        }

        final AtomixBootNodeConfiguration.Service service = configuration.getService();
        final List<Node> nodes = new ArrayList();

        if (discoveryClient != null && service.getName() != null) {
            discoveryClient.getInstances(service.getName()).stream()
                .map(s -> Endpoint.from(s.getHost(), s.getPort()))
                .map(e -> Node.builder().withType(Node.Type.DATA).withEndpoint(e).build())
                .forEach(nodes::add);
        }

        if (service.getNodes() != null) {
            service.getNodes().stream()
                .map(AtomixUtil::asEndpoint)
                .filter(Optional::isPresent)
                .map(e -> Node.builder().withType(Node.Type.DATA).withEndpoint(e.get()).build())
                .forEach(nodes::add);
        }

        builder.withBootstrapNodes(nodes);

        return new AtomixBootNode(
            builder.withLocalNode(
                nodeBuilder.withEndpoint(endpoint).build()
            ).build(),
            Optional.ofNullable(service.getName()),
            Optional.ofNullable(serviceRegistry)
        );
    }
}
