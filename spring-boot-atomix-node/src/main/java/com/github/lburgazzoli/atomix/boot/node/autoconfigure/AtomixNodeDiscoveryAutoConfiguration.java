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
package com.github.lburgazzoli.atomix.boot.node.autoconfigure;

import com.github.lburgazzoli.atomix.boot.autoconfigure.AtomixAutoConfiguration;
import com.github.lburgazzoli.atomix.boot.autoconfigure.AtomixConfiguration;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@AutoConfigureBefore(AtomixAutoConfiguration.class)
@Configuration
@ConditionalOnProperty(prefix = "atomix.discovery.k8s", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AtomixConfiguration.class)
public class AtomixNodeDiscoveryAutoConfiguration {

    @ConditionalOnMissingBean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-k8s-discovery")
    public NodeDiscoveryProvider atomixK8sDiscovery(AtomixNodeDiscoveryConfiguration configuration) {
        AtomixNodeDiscoveryProvider.Config config = new AtomixNodeDiscoveryProvider.Config();
        config.setNamnespace(configuration.getNamespace());
        config.setEndpointName(configuration.getEndpointName());
        config.setPortName(configuration.getPortName());

        return new AtomixNodeDiscoveryProvider(config);
    }
}
