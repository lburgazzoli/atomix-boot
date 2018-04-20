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
package com.github.lburgazzoli.atomix.boot.client;

import io.atomix.core.Atomix;
import io.atomix.core.AtomixConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnClass(name = "io.atomix.core.Atomix")
@ConditionalOnProperty(value = "atomix.client.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AtomixBootClientConfiguration.class)
public class AtomixBootClientAutoConfiguration {
    @Autowired
    private AtomixBootClientConfiguration configuration;

    @Lazy
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-client", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(Atomix.class)
    public Atomix atomixClient() {
        AtomixConfig config = new AtomixConfig();
        config.setDataDirectory(configuration.getDataDirectory());
        config.getClusterConfig().setLocalNode(configuration.getLocalNode());
        config.getClusterConfig().setName(configuration.getCluster().getName());
        config.getClusterConfig().setNodes(configuration.getCluster().getNodes());

        return Atomix.builder(config).build();
    }
}
