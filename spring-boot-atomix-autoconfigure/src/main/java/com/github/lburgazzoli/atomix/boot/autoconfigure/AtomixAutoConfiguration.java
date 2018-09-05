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
package com.github.lburgazzoli.atomix.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.lburgazzoli.atomix.boot.AtomixInstance;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnProperty(prefix = "atomix", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtomixConfiguration.class)
public class AtomixAutoConfiguration {

    @ConditionalOnMissingBean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-instance", initMethod = "start", destroyMethod = "stop")
    public AtomixInstance atomix(
            AtomixConfiguration configuration,
            Optional<List<AtomixInstance.Listener>> listeners,
            NodeDiscoveryProvider discoveryProvider) {

        final String clusterId = configuration.getClusterId();
        final String memberId = configuration.getMember().getId();

        final Atomix atomix = Atomix.builder()
            .withClusterId(clusterId)
            .withMemberId(memberId)
            .withAddress(configuration.getMember().getHost(), configuration.getMember().getPort())
            .withMembershipProvider(discoveryProvider)
            .withProfiles(Profile.dataGrid())
            .build();

        return new AtomixInstance(
            atomix,
            listeners.orElseGet(Collections::emptyList)
        );
    }

    @ConditionalOnMissingBean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-bootstrap-discovery")
    public NodeDiscoveryProvider atomixBootstrapDiscovery(AtomixConfiguration configuration) {
        List<Node> nodes = new ArrayList<>();

        for (AtomixConfiguration.Member member: configuration.getNodes()) {
            nodes.add(
                Node.builder()
                    .withId(member.getId())
                    .withAddress(member.getHost(), member.getPort())
                    .build()
            );
        }

        return BootstrapDiscoveryProvider.builder().withNodes(nodes).build();
    }
}
