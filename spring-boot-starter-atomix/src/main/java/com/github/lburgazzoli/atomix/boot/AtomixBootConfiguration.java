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
package com.github.lburgazzoli.atomix.boot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.atomix.cluster.MemberConfig;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroupConfig;
import io.atomix.protocols.raft.partition.RaftPartitionGroupConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties("atomix")
public abstract class AtomixBootConfiguration {
    private boolean enabled = true;

    private File configurationPath;

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private MemberConfig localMember;

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private Cluster cluster;

    private List<AtomixProfile> profiles = new ArrayList<>();

    @Valid
    @NestedConfigurationProperty
    private PartitionsGroups partitionGroups = new PartitionsGroups();

    // ***************
    // Properties
    // ***************

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public File getConfigurationPath() {
        return configurationPath;
    }

    public void setConfigurationPath(File configurationPath) {
        this.configurationPath = configurationPath;
    }

    public boolean hasConfigurationPath() {
        return this.configurationPath != null && this.configurationPath.exists();
    }

    public MemberConfig getLocalMember() {
        return localMember;
    }

    public void setLocalMember(MemberConfig localMember) {
        this.localMember = localMember;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public List<AtomixProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<AtomixProfile> profiles) {
        this.profiles = profiles;
    }

    public PartitionsGroups getPartitionGroups() {
        return partitionGroups;
    }


    public void setPartitionGroups(PartitionsGroups partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    // ***************
    //
    // ***************

    public static class Cluster {
        /**
         * The name of the cluster.
         */
        @NotNull
        private String name;

        /**
         * The nodes composing the cluster.
         */
        private List<MemberConfig> members = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<MemberConfig> getMembers() {
            return members;
        }

        public void setMembers(List<MemberConfig> members) {
            this.members = members;
        }
    }

    public static class PartitionsGroups {
        /**
         * Raft partitions.
         */
        private List<RaftPartitionGroupConfig> raft = new ArrayList<>();

        /**
         * PrimaryBackup partitions.
         */
        private List<PrimaryBackupPartitionGroupConfig> primaryBackup = new ArrayList<>();

        public List<RaftPartitionGroupConfig> getRaft() {
            return raft;
        }

        public List<PrimaryBackupPartitionGroupConfig> getPrimaryBackup() {
            return primaryBackup;
        }
    }
}
