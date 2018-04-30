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
package com.github.lburgazzoli.atomix.cloud.node;

import com.github.lburgazzoli.atomix.boot.AtomixBoot;
import com.github.lburgazzoli.atomix.boot.AtomixConfigurationCustomizer;
import com.github.lburgazzoli.atomix.boot.AtomixUtils;
import io.atomix.cluster.ClusterMembershipEvent;
import io.atomix.cluster.ClusterMembershipEventListener;
import io.atomix.cluster.Member;
import io.atomix.cluster.MemberConfig;
import io.atomix.core.Atomix;
import io.atomix.core.AtomixConfig;
import io.atomix.utils.net.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AtomixCloudNodeApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixCloudNodeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AtomixCloudNodeApplication.class, args);
    }

    @Bean
    public AtomixBoot.Listener memberListener() {
        return new AtomixBootListener();
    }

    @Bean
    public AtomixConfigurationCustomizer memberDiscovery(DiscoveryClient discoveryClient) {
        return new AtomixConfigurationCustomizer() {
            @Override
            public void customize(AtomixConfig target) {
                String clusterName = target.getClusterConfig().getName();

                if (discoveryClient != null && clusterName != null) {
                    for (ServiceInstance instance: discoveryClient.getInstances(clusterName)) {
                        final String id = instance.getMetadata().get(AtomixUtils.META_MEMBER_ID);
                        final String type = instance.getMetadata().get(AtomixUtils.META_MEMBER_TYPE);

                        if (type == null) {
                            continue;
                        }

                        MemberConfig memberConfig = new MemberConfig();
                        memberConfig.setAddress(Address.from(instance.getHost(), instance.getPort()));
                        memberConfig.setType(Member.Type.valueOf(type));

                        if (id != null) {
                            memberConfig.setId(id);
                        }

                        target.getClusterConfig().addMember(memberConfig);
                    }
                }

            }
        };
    }


    private static class AtomixBootListener implements AtomixBoot.Listener, ClusterMembershipEventListener {
        @Override
        public void started(Atomix atomix) {
            atomix.membershipService().addListener(this);
        }

        @Override
        public void stopped(Atomix atomix) {
            atomix.membershipService().removeListener(this);
        }

        @Override
        public void onEvent(ClusterMembershipEvent event) {
            LOGGER.info("OnEvent: type={}, subject={}", event.type(), event.subject());
        }
    }
}
