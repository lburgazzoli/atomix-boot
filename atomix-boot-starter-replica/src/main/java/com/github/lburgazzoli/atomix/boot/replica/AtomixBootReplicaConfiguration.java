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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.copycat.server.storage.StorageLevel;
import io.atomix.resource.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("atomix.replica")
public class AtomixBootReplicaConfiguration {
    /**
     * Enable the replica auto configuration.
     */
    private boolean enabled = true;

    /**
     * Enable cluster boostrapping.
     */
    private boolean bootstrap = true;

    /**
     * The address through which clients and replicas connect to the replica
     */
    private Address address;

    /**
     * The bootstrap cluster configuration
     */
    private List<Address> nodes = new ArrayList<>();

    /**
     * The transport type.
     */
    private Class<? extends Transport> transportType;

    /**
     * The server transport type.
     */
    private Class<? extends Transport> serverTransportType;

    /**
     * The client transport type.
     */
    private Class<? extends Transport> clientTransportType;

    /**
     * The resource types.
     */
    private List<Class<? extends Resource<?>>> resourceTypes = new ArrayList<>();

    /**
     * The replica election timeout
     */
    private Long electionTimeout;

    /**
     * The replica's global suspend timeout
     */
    private Long globalSuspendTimeout;

    /**
     * The replica heartbeat interval.
     */
    private Long heartbeatInterval;

    /**
     * The replica session timeout;
     */
    private Long sessionTimeout;

    /**
     * The member type;
     */
    private AtomixReplica.Type type;

    @NestedConfigurationProperty
    private Storage storage;

    // ****************************************
    // Properties
    // ****************************************

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(boolean bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Address> getNodes() {
        return nodes;
    }

    public Class<? extends Transport> getTransportType() {
        return transportType;
    }

    public void setTransportType(Class<? extends Transport> transportType) {
        this.transportType = transportType;
    }

    public Class<? extends Transport> getServerTransportType() {
        return serverTransportType;
    }

    public void setServerTransportType(Class<? extends Transport> serverTransportType) {
        this.serverTransportType = serverTransportType;
    }

    public Class<? extends Transport> getClientTransportType() {
        return clientTransportType;
    }

    public void setClientTransportType(Class<? extends Transport> clientTransportType) {
        this.clientTransportType = clientTransportType;
    }

    public List<Class<? extends Resource<?>>> getResourceTypes() {
        return resourceTypes;
    }

    public Long getElectionTimeout() {
        return electionTimeout;
    }

    public void setElectionTimeout(Long electionTimeout) {
        this.electionTimeout = electionTimeout;
    }

    public Long getGlobalSuspendTimeout() {
        return globalSuspendTimeout;
    }

    public void setGlobalSuspendTimeout(Long globalSuspendTimeout) {
        this.globalSuspendTimeout = globalSuspendTimeout;
    }

    public Long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public AtomixReplica.Type getType() {
        return type;
    }

    public void setType(AtomixReplica.Type type) {
        this.type = type;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    // ****************************************
    // Nested config
    // ****************************************

    public static class Storage {
        /**
         * The log storage level.
         */
        private StorageLevel level;

        /**
         * The log storage directory.
         */
        private File directtory;

        /**
         * The number of log compaction threads.
         */
        private Integer compactionThreads;

        /**
         * The percentage of entries in the segment that must be released before a segment can be compacted.
         */
        private Double compactionThreshold;

        /**
         * Enables flushing buffers to disk when entries are committed to a segment.
         */
        private Boolean flushOnCommit;

        /**
         * The entry buffer size.
         */
        private Integer entryBufferSize;

        /**
         * The maximum number of allows entries per segment.
         */
        private Integer maxEntriesPerSegment;

        /**
         * The maximum segment size in bytes.
         */
        private Integer maxSegmentSize;

        /**
         * The major compaction interval (in milliseconds).
         */
        private Long majorCompactionInterval;

        /**
         * The minor compaction interval (in milliseconds).
         */
        private Long minorCompactionInterval;

        /**
         * Enables retaining stale snapshots on disk.
         */
        private Boolean retainStaleSnapshots;

        // ********************************************
        // Properties
        // ********************************************

        public StorageLevel getLevel() {
            return level;
        }

        public void setLevel(StorageLevel level) {
            this.level = level;
        }

        public File getDirecttory() {
            return directtory;
        }

        public void setDirecttory(File directtory) {
            this.directtory = directtory;
        }

        public Integer getCompactionThreads() {
            return compactionThreads;
        }

        public void setCompactionThreads(Integer compactionThreads) {
            this.compactionThreads = compactionThreads;
        }

        public Double getCompactionThreshold() {
            return compactionThreshold;
        }

        public void setCompactionThreshold(Double compactionThreshold) {
            this.compactionThreshold = compactionThreshold;
        }

        public Boolean getFlushOnCommit() {
            return flushOnCommit;
        }

        public void setFlushOnCommit(Boolean flushOnCommit) {
            this.flushOnCommit = flushOnCommit;
        }

        public Integer getEntryBufferSize() {
            return entryBufferSize;
        }

        public void setEntryBufferSize(Integer entryBufferSize) {
            this.entryBufferSize = entryBufferSize;
        }

        public Integer getMaxEntriesPerSegment() {
            return maxEntriesPerSegment;
        }

        public void setMaxEntriesPerSegment(Integer maxEntriesPerSegment) {
            this.maxEntriesPerSegment = maxEntriesPerSegment;
        }

        public Integer getMaxSegmentSize() {
            return maxSegmentSize;
        }

        public void setMaxSegmentSize(Integer maxSegmentSize) {
            this.maxSegmentSize = maxSegmentSize;
        }

        public Long getMajorCompactionInterval() {
            return majorCompactionInterval;
        }

        public void setMajorCompactionInterval(Long majorCompactionInterval) {
            this.majorCompactionInterval = majorCompactionInterval;
        }

        public Long getMinorCompactionInterval() {
            return minorCompactionInterval;
        }

        public void setMinorCompactionInterval(Long minorCompactionInterval) {
            this.minorCompactionInterval = minorCompactionInterval;
        }

        public Boolean getRetainStaleSnapshots() {
            return retainStaleSnapshots;
        }

        public void setRetainStaleSnapshots(Boolean retainStaleSnapshots) {
            this.retainStaleSnapshots = retainStaleSnapshots;
        }
    }
}
