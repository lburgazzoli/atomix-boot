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
package com.github.lburgazzoli.atomix.boot.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;

import io.atomix.cluster.NodeConfig;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

public abstract class AtomixBootConfiguration {
    private boolean enabled = true;

    @Valid
    @Nonnull
    @NestedConfigurationProperty
    private NodeConfig localNode;

    @Nullable
    private File dataDirectory;

    @Valid
    @Nonnull
    @NestedConfigurationProperty
    private Cluster cluster;

    // ***************
    // Properties
    // ***************

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public NodeConfig getLocalNode() {
        return localNode;
    }

    public void setLocalNode(NodeConfig localNode) {
        this.localNode = localNode;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    // ***************
    //
    // ***************

    public static class Cluster {
        /**
         * The name of the cluster.
         */
        @Nonnull
        public String name;

        /**
         * The nodes composing the cluster.
         */
        public List<NodeConfig> nodes = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<NodeConfig> getNodes() {
            return nodes;
        }

        public void setNodes(List<NodeConfig> nodes) {
            this.nodes = nodes;
        }
    }
}
