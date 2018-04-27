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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.lburgazzoli.atomix.boot.common.AtomixBootNodeRegistry;
import com.github.lburgazzoli.atomix.boot.common.AtomixBootUtils;
import io.atomix.cluster.Node;
import io.atomix.cluster.NodeConfig;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixConfig;
import io.atomix.primitive.partition.PartitionGroupConfig;
import io.atomix.utils.net.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnClass(name = "io.atomix.core.Atomix")
@ConditionalOnProperty(prefix = "atomix.node", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AtomixBootNodeConfiguration.class)
public class AtomixBootNodeAutoConfiguration {
    @Autowired
    private AtomixBootNodeConfiguration configuration;
    @Autowired(required = false)
    private DiscoveryClient discoveryClient;
    @Autowired(required = false)
    private AtomixBootNodeRegistry nodeRegistry;

    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-node", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(AtomixBootNode.class)
    public AtomixBootNode atomixNode() {
        final String clusterName = configuration.getCluster().getName();
        final List<NodeConfig> clusterNodes = new ArrayList<>();

        // local node
        clusterNodes.add(configuration.getLocalNode());

        // first add statically configured nodes
        clusterNodes.addAll(configuration.getCluster().getNodes());

        // then use DiscoveryClient to discovery additional nodes
        if (discoveryClient != null && clusterName != null) {
            for (ServiceInstance instance: discoveryClient.getInstances(clusterName)) {
                String type = instance.getMetadata().getOrDefault(AtomixBootUtils.META_NODE_TYPE, "CORE");
                String id = instance.getMetadata().get(AtomixBootUtils.META_NODE_ID);

                NodeConfig nodeConfig = new NodeConfig();
                nodeConfig.setAddress(Address.from(instance.getHost(), instance.getPort()));
                nodeConfig.setType(Node.Type.valueOf(type));

                if (id != null) {
                    nodeConfig.setId(id);
                }

                clusterNodes.add(nodeConfig);
            }
        }

        AtomixConfig config = new AtomixConfig();
        config.setDataDirectory(configuration.getDataDirectory());
        config.getClusterConfig().setLocalNode(configuration.getLocalNode());
        config.getClusterConfig().setName(configuration.getCluster().getName());
        config.getClusterConfig().setNodes(clusterNodes);

        List<PartitionGroupConfig> partitions = new ArrayList<>();
        configuration.getPartitionGroups().getRaft().forEach(partitions::add);
        configuration.getPartitionGroups().getPrimaryBackup().forEach(partitions::add);

        config.setPartitionGroups(partitions);

        return new AtomixBootNode(
            Atomix.builder(config).build(),
            Optional.ofNullable(clusterName),
            Optional.ofNullable(nodeRegistry)
        );
    }
}
