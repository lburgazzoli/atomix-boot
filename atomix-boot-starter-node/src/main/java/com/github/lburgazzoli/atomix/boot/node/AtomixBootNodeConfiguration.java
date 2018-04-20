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
package com.github.lburgazzoli.atomix.boot.node;

import io.atomix.core.AtomixConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("atomix.node")
public class AtomixBootNodeConfiguration extends AtomixConfig {
    /**
     * Enable the replica auto configuration.
     */
    private boolean enabled = true;

    /*
    @NotNull
    private Node node;

    @Valid
    @NestedConfigurationProperty
    private AtomixBootNodeConfiguration.Cluster cluster = new Cluster();

    @Valid
    @NestedConfigurationProperty
    private Storage storage = new Storage();
    */


    //@Valid
    //@NestedConfigurationProperty
    //private AtomixConfig cfg = new AtomixConfig();

    // ****************************************
    // Properties
    // ****************************************

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /*
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Storage getStorage() {
        return storage;
    }
    */

    //public AtomixConfig getCfg() {
    //    return cfg;
    //}

    /*
    public void setCfg(AtomixConfig cfg) {
        this.cfg = cfg;
    }
    */

    // ****************************************
    // Nested config
    // ****************************************

    /*
    public static class Node {
        @Nonnull
        public String name;

        @Nonnull
        public io.atomix.cluster.Node.Type type;

        @Nonnull
        public String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public io.atomix.cluster.Node.Type getType() {
            return type;
        }

        public void setType(io.atomix.cluster.Node.Type type) {
            this.type = type;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Storage {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Cluster {
        @Nonnull
        private String name;

        @Nonnull
        private List<Node> nodes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
        }
    }
    */
}
