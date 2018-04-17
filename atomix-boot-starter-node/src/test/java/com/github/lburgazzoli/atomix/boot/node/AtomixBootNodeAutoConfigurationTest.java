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
import java.util.Properties;

import io.atomix.core.Atomix;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

@Disabled
public class AtomixBootNodeAutoConfigurationTest {

    @Test
    public void testValidationFailure() {
        Properties properties = new Properties();
        properties.put("debug", false);
        properties.put("spring.main.banner-mode", Banner.Mode.OFF);

        Assertions.assertThatThrownBy(
            () -> new SpringApplicationBuilder()
                .properties(properties)
                .sources(AtomixBootNodeAutoConfiguration.class)
                .run()
        ).hasMessageContaining(
            "Field error in object 'atomix.replica' on field 'address': rejected value [null]"
        ).isInstanceOf(
            UnsatisfiedDependencyException.class
        );
    }

    @Test
    public void testReplica() throws IOException {
        final Path tmp = Files.createTempDirectory("path");

        Properties properties = new Properties();
        properties.put("debug", false);
        properties.put("spring.main.banner-mode", Banner.Mode.OFF);
        properties.put("atomix.replica.address", "localhost:" + SocketUtils.findAvailableTcpPort());
        properties.put("atomix.replica.storage.path", tmp.toFile().getAbsolutePath());

        ConfigurableApplicationContext context = null;

        try {
            context = new SpringApplicationBuilder()
                .properties(properties)
                .sources(AtomixBootNodeAutoConfiguration.class)
                .run();

            Assertions.assertThat(context).isNotNull();
            Assertions.assertThat(context.getBean(Atomix.class)).isNotNull();
        } finally {
            if (context != null) {
                context.close();
            }

            Files.delete(tmp);
        }
    }
}
