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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import io.atomix.cluster.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.SocketUtils;

public class AtomixBootNodeAutoConfigurationTest {
    @Disabled
    @Test
    public void testValidationFailure() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AtomixBootNodeAutoConfiguration.class))
            .withPropertyValues(
                "debug=false",
                "spring.main.banner-mode=off")
            .run(
                context -> {
                    Assertions.assertThat(context).isNotNull();
                    Assertions.assertThat(context).getFailure().isInstanceOf(UnsatisfiedDependencyException.class);
                }
            );
    }

    @Test
    public void testNode() throws IOException {
        final Path tmp = Files.createTempDirectory("path");

        try {
            new ApplicationContextRunner()
                .withConfiguration(
                    AutoConfigurations.of(AtomixBootNodeAutoConfiguration.class))
                .withPropertyValues(
                    "debug=false",
                    "spring.main.banner-mode=off",
                    "atomix.node.local-member.id=" + UUID.randomUUID().toString(),
                    "atomix.node.local-member.type=" + Member.Type.EPHEMERAL.name(),
                    "atomix.node.local-member.address=localhost:" + SocketUtils.findAvailableTcpPort(),
                    "atomix.node.cluster.name=" + "cluster",
                    "atomix.node.partition-groups.raft[0].name=" + "raft",
                    "atomix.node.partition-groups.raft[0].partitions=" + "3",
                    "atomix.node.partition-groups.raft[0].partition-size=" + "3")
                .run(
                    context -> {
                        Assertions.assertThat(context).isNotNull();
                        Assertions.assertThat(context).hasSingleBean(AtomixBootNode.class);
                    }
                );
        } finally {
            FileSystemUtils.deleteRecursively(tmp);
        }
    }
}
