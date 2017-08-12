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
package com.github.lburgazzoli.atomix.boot.replica;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.lburgazzoli.atomix.boot.common.SerializerCustomizer;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.buffer.PooledHeapAllocator;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import io.atomix.cluster.ClusterManager;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.resource.ResourceType;
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
@ConditionalOnClass(AtomixReplica.class)
@ConditionalOnProperty(value = "atomix.replica.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AtomixBootReplicaConfiguration.class)
public class AtomixBootReplicaAutoConfiguration {
    @Autowired
    private AtomixBootReplicaConfiguration configuration;
    @Autowired(required = false)
    private List<ResourceType> resourceTypes = Collections.emptyList();
    @Autowired(required = false)
    private ClusterManager clusterManager;
    @Autowired(required = false)
    private Serializer serializer;
    @Autowired(required = false)
    private List<SerializerCustomizer> serializerCustomizers = Collections.emptyList();

    @Lazy
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean(name = "atomix-replica", destroyMethod = "shutdown")
    @ConditionalOnMissingBean(AtomixReplica.class)
    public AtomixReplica atomixReplica() throws Exception {
        AtomixReplica.Builder builder = AtomixReplica.builder(configuration.getAddress());

        // Storage
        AtomixBootReplicaConfiguration.Storage storageConf = configuration.getStorage();
        if (storageConf != null) {
            Storage.Builder storageBuilder = Storage.builder();

            Optional.ofNullable(storageConf.getFlushOnCommit()).ifPresent(storageBuilder::withFlushOnCommit);
            Optional.ofNullable(storageConf.getCompactionThreads()).ifPresent(storageBuilder::withCompactionThreads);
            Optional.ofNullable(storageConf.getCompactionThreshold()).ifPresent(storageBuilder::withCompactionThreshold);
            Optional.ofNullable(storageConf.getDirectory()).ifPresent(storageBuilder::withDirectory);
            Optional.ofNullable(storageConf.getLevel()).ifPresent(storageBuilder::withStorageLevel);
            Optional.ofNullable(storageConf.getEntryBufferSize()).ifPresent(storageBuilder::withEntryBufferSize);
            Optional.ofNullable(storageConf.getMaxSegmentSize()).ifPresent(storageBuilder::withMaxSegmentSize);
            Optional.ofNullable(storageConf.getMaxEntriesPerSegment()).ifPresent(storageBuilder::withMaxEntriesPerSegment);
            Optional.ofNullable(storageConf.getMajorCompactionInterval()).map(Duration::ofMillis).ifPresent(storageBuilder::withMajorCompactionInterval);
            Optional.ofNullable(storageConf.getMinorCompactionInterval()).map(Duration::ofMillis).ifPresent(storageBuilder::withMinorCompactionInterval);
            Optional.ofNullable(storageConf.getRetainStaleSnapshots()).ifPresent(storageBuilder::withRetainStaleSnapshots);

            builder.withStorage(storageBuilder.build());
        }

        // Transport
        if (configuration.getClientTransportType() != null) {
            Class<? extends Transport> type = configuration.getClientTransportType();
            builder.withClientTransport(type.newInstance());
        }
        if (configuration.getServerTransportType() != null) {
            Class<? extends Transport> type = configuration.getServerTransportType();
            builder.withServerTransport(type.newInstance());
        }
        if (configuration.getTransportType() != null) {
            Class<? extends Transport> type = configuration.getTransportType();
            builder.withTransport(type.newInstance());
        }

        // Resources
        builder.withResourceTypes(configuration.getResourceTypes().stream().map(ResourceType::new).collect(Collectors.toList()));
        builder.withResourceTypes(resourceTypes);

        // Timeouts
        Optional.ofNullable(configuration.getElectionTimeout()).map(Duration::ofMillis).ifPresent(builder::withElectionTimeout);
        Optional.ofNullable(configuration.getGlobalSuspendTimeout()).map(Duration::ofMillis).ifPresent(builder::withGlobalSuspendTimeout);
        Optional.ofNullable(configuration.getHeartbeatInterval()).map(Duration::ofMillis).ifPresent(builder::withHeartbeatInterval);
        Optional.ofNullable(configuration.getSessionTimeout()).map(Duration::ofMillis).ifPresent(builder::withSessionTimeout);

        // Misc
        Optional.ofNullable(configuration.getType()).ifPresent(builder::withType);
        Optional.ofNullable(clusterManager).ifPresent(builder::withClusterManager);

        Serializer serializer = this.serializer;
        if (serializer == null) {
            serializer = new Serializer(new PooledHeapAllocator());
        }
        for (SerializerCustomizer customizer: serializerCustomizers) {
            customizer.customize(serializer);
        }

        builder.withSerializer(serializer);

        return configuration.isBootstrap()
            ? builder.build().bootstrap(configuration.getNodes()).join()
            : builder.build();
    }
}
