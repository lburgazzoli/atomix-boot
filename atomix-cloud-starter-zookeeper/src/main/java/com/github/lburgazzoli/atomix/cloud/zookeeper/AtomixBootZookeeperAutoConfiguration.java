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
package com.github.lburgazzoli.atomix.cloud.zookeeper;

import com.github.lburgazzoli.atomix.boot.common.AtomixBootNodeRegistry;
import com.github.lburgazzoli.atomix.boot.common.AtomixBootUtils;
import com.github.lburgazzoli.atomix.boot.common.DelegatingAtomixBootNodeRegistry;
import com.github.lburgazzoli.atomix.boot.node.AtomixBootNodeAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(ZookeeperServiceRegistryAutoConfiguration.class)
@AutoConfigureBefore(AtomixBootNodeAutoConfiguration.class)
@ConditionalOnZookeeperEnabled
public class AtomixBootZookeeperAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ZookeeperServiceRegistry.class, ZookeeperDiscoveryProperties.class})
    public AtomixBootNodeRegistry zookeeperNodeRegistry(
            final ZookeeperServiceRegistry serviceRegistry,
            final ZookeeperDiscoveryProperties discoveryProperties) {

        return new DelegatingAtomixBootNodeRegistry<>(serviceRegistry, registration -> {
            ZookeeperInstance instance = new ZookeeperInstance(
                registration.getMetadata().get(AtomixBootUtils.META_NODE_ID),
                registration.getServiceId(),
                registration.getMetadata()
            );

            return ServiceInstanceRegistration.builder()
                .address(registration.getHost())
                .port(registration.getPort())
                .name(registration.getServiceId())
                .payload(instance)
                .uriSpec(discoveryProperties.getUriSpec())
                .build();
            }
        );
    }
}
