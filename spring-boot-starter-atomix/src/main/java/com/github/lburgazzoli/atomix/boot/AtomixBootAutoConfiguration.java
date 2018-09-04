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
package com.github.lburgazzoli.atomix.boot;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.atomix.core.Atomix;
import io.atomix.core.AtomixConfig;
import io.atomix.utils.config.Configs;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnClass(name = "io.atomix.core.Atomix")
@ConditionalOnProperty(prefix = "atomix.node", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AtomixBootConfiguration.class)
public class AtomixBootAutoConfiguration {

    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(AtomixInstance.class)
    public AtomixInstance atomix(
            AtomixBootConfiguration configuration,
            Optional<List<AtomixInstance.Listener>> listeners,
            Optional<List<AtomixConfigurationCustomizer>> customizers) {

        final AtomixConfig config;

        if (configuration.getConfigurationPath() != null) {
            config = Configs.load(configuration.getConfigurationPath(), AtomixConfig.class);
        } else {
            config = new AtomixConfig();
        }

        // common conf
        config.getClusterConfig().setLocalMember(configuration.getLocalMember());
        config.getClusterConfig().getLocalMember().addTag(String.format("%s=%s", AtomixUtils.META_MEMBER_CLUSTER_ID, configuration.getCluster().getName()));
        config.getClusterConfig().setName(configuration.getCluster().getName());

        // local member
        config.getClusterConfig().addMember(configuration.getLocalMember());

        // first add statically configured nodes
        configuration.getCluster().getMembers().forEach(config.getClusterConfig()::addMember);

        /*
        // then use DiscoveryClient to discovery additional nodes
        if (discoveryClient != null && clusterName != null) {
            for (ServiceInstance instance: discoveryClient.getInstances(clusterName)) {
                final String id = instance.getMetadata().get(AtomixBootUtils.META_NODE_ID);
                final String type = instance.getMetadata().get(AtomixBootUtils.META_NODE_TYPE);

                if (type == null) {
                    continue;
                }

                MemberConfig memberConfig = new MemberConfig();
                memberConfig.setAddress(Address.from(instance.getHost(), instance.getPort()));
                memberConfig.setType(Member.Type.valueOf(type));

                if (id != null) {
                    memberConfig.setId(id);
                }

                config.getClusterConfig().addMember(memberConfig);
            }
        }
        */

        // Partitions
        configuration.getPartitionGroups().getRaft().forEach(config::addPartitionGroup);
        configuration.getPartitionGroups().getPrimaryBackup().forEach(config::addPartitionGroup);

        // Profiles
        configuration.getProfiles().stream().map(AtomixProfile::value).forEach(config::addProfile);

        // Apply customizers
        customizers.ifPresent(c -> {
            for (AtomixConfigurationCustomizer customizer : c) {
                customizer.customize(config);
            }
        });

        return new AtomixInstance(
            Atomix.builder(config).build(),
            listeners.orElseGet(Collections::emptyList)
        );
    }
}
