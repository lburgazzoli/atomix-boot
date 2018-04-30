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
package com.github.lburgazzoli.atomix.cloud.node.consul;

import java.util.stream.Collectors;

import com.ecwid.consul.v1.agent.model.NewService;
import com.github.lburgazzoli.atomix.boot.common.AtomixBootNodeRegistry;
import com.github.lburgazzoli.atomix.boot.common.AtomixBootUtils;
import com.github.lburgazzoli.atomix.boot.common.DelegatingAtomixBootNodeRegistry;
import com.github.lburgazzoli.atomix.boot.node.AtomixBootNodeAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(ConsulServiceRegistryAutoConfiguration.class)
@AutoConfigureBefore(AtomixBootNodeAutoConfiguration.class)
@ConditionalOnConsulEnabled
public class AtomixBootNodeConsulAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ConsulServiceRegistry.class, ConsulDiscoveryProperties.class})
    public AtomixBootNodeRegistry consulNodeRegistry(
            final ConsulServiceRegistry serviceRegistry,
            final ConsulDiscoveryProperties discoveryProperties) {

        return new DelegatingAtomixBootNodeRegistry<>(serviceRegistry, registration -> {
                NewService service = new NewService();
                service.setId(registration.getMetadata().get(AtomixBootUtils.META_NODE_ID));
                service.setName(registration.getServiceId());
                service.setAddress(registration.getHost());
                service.setPort(registration.getPort());
                service.setTags(
                    registration.getMetadata().entrySet().stream()
                        .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                );

                return new ConsulRegistration(service, discoveryProperties);
            }
        );
    }
}
