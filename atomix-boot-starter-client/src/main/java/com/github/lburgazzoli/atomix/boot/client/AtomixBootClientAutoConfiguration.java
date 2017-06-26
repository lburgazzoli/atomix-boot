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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.lburgazzoli.atomix.boot.common.SerializerCustomizer;
import io.atomix.AtomixClient;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.buffer.PooledHeapAllocator;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.resource.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(AtomixReplica.class)
@ConditionalOnProperty(value = "atomix.client.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AtomixBootClientConfiguration.class)
public class AtomixBootClientAutoConfiguration {
    @Autowired
    private AtomixBootClientConfiguration configuration;
    @Autowired(required = false)
    private List<ResourceType> resourceTypes = Collections.emptyList();
    @Autowired(required = false)
    private Serializer serializer;
    @Autowired(required = false)
    private List<SerializerCustomizer> serializerCustomizers = Collections.emptyList();

    @ConditionalOnMissingBean(AtomixClient.class)
    @Bean(name = "atomix-client")
    public AtomixClient atomixReplica() throws Exception {
        AtomixClient.Builder builder = AtomixClient.builder();

        if (configuration.getTransportType() != null) {
            Class<? extends Transport> type = configuration.getTransportType();
            builder.withTransport(type.newInstance());
        }

        // Resources
        builder.withResourceTypes(configuration.getResourceTypes().stream().map(ResourceType::new).collect(Collectors.toList()));
        builder.withResourceTypes(resourceTypes);

        // Timeouts
        Optional.ofNullable(configuration.getSessionTimeout()).map(Duration::ofMillis).ifPresent(builder::withSessionTimeout);


        // Misc
        Serializer serializer = this.serializer;
        if (serializer == null) {
            serializer = new Serializer(new PooledHeapAllocator());
        }
        for (SerializerCustomizer customizer: serializerCustomizers) {
            customizer.customize(serializer);
        }

        builder.withSerializer(serializer);

        AtomixClient client = builder.build();
        client.connect(configuration.getNodes()).join();

        return client;
    }
}
