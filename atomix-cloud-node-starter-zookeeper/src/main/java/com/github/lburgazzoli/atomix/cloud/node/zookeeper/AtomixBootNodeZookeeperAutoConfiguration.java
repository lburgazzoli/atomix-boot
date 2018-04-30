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
package com.github.lburgazzoli.atomix.cloud.node.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.lburgazzoli.atomix.boot.AtomixBoot;
import com.github.lburgazzoli.atomix.boot.AtomixBootAutoConfiguration;
import com.github.lburgazzoli.atomix.boot.AtomixBootConfiguration;
import com.github.lburgazzoli.atomix.boot.AtomixUtils;
import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(ZookeeperServiceRegistryAutoConfiguration.class)
@AutoConfigureBefore(AtomixBootAutoConfiguration.class)
@ConditionalOnZookeeperEnabled
public class AtomixBootNodeZookeeperAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixBootNodeZookeeperAutoConfiguration.class);

    @Bean
    @ConditionalOnBean({ZookeeperServiceRegistry.class, ZookeeperDiscoveryProperties.class})
    public AtomixBoot.Listener registerAsZookeeperServiceListener(
        final ZookeeperServiceRegistry serviceRegistry,
        final ZookeeperDiscoveryProperties discoveryProperties) {

        return new AtomixBoot.Listener() {
            @Override
            public void started(Atomix atomix) {
                final Member local = atomix.membershipService().getLocalMember();
                final Optional<ZookeeperRegistration> registration = nativeRegistration(local, discoveryProperties);

                registration.ifPresent(r -> {
                    LOGGER.info("Register member: {} to registry: {}", local, serviceRegistry);
                    serviceRegistry.register(r);
                });
            }

            @Override
            public void stopped(Atomix atomix) {
                final Member local = atomix.membershipService().getLocalMember();
                final Optional<ZookeeperRegistration> registration = nativeRegistration(local, discoveryProperties);

                registration.ifPresent( r -> {
                    LOGGER.info("De-register member: {} from registry: {}", local, serviceRegistry);
                    serviceRegistry.deregister(r);
                });

            }
        };
    }

    private static Optional<ZookeeperRegistration> nativeRegistration(Member member, ZookeeperDiscoveryProperties discoveryProperties)  {
        return AtomixUtils.getClusterId(member)
            .map(clusterId -> {                    // add tags
                    List<String> tags = new ArrayList<>();

                    // extract simple tags
                    AtomixUtils.getTags(member).stream()
                        .forEach(tags::add);

                    // transform metadata to tags
                    AtomixUtils.getMeta(member).entrySet().stream()
                        .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                        .forEach(tags::add);

                    ZookeeperInstance instance = new ZookeeperInstance(
                        clusterId,
                        member.id().id(),
                        AtomixUtils.getMeta(member)
                    );

                    return ServiceInstanceRegistration.builder()
                        .address(member.address().host())
                        .port(member.address().port())
                        .name(clusterId)
                        .payload(instance)
                        .uriSpec(discoveryProperties.getUriSpec())
                        .build();
                }
            );
    }
}
