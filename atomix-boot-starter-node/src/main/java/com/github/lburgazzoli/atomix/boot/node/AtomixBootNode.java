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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.lburgazzoli.atomix.boot.common.AtomixBootNodeRegistration;
import com.github.lburgazzoli.atomix.boot.common.AtomixBootNodeRegistry;
import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

public final class AtomixBootNode implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixBootNode.class);

    private final AtomicBoolean running;
    private final Atomix atomix;
    private final Optional<String> serviceId;
    private final Optional<AtomixBootNodeRegistry> nodeRegistry;

    public AtomixBootNode(Atomix atomix, Optional<String> serviceId, Optional<AtomixBootNodeRegistry> nodeRegistry) {
        this.running = new AtomicBoolean(false);
        this.atomix = atomix;
        this.serviceId = serviceId;
        this.nodeRegistry = nodeRegistry;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            atomix.start().thenAccept(
                a -> {
                    LOGGER.info("Node {} started", atomix.membershipService().getLocalMember().id());

                    if (!serviceId.isPresent() || !nodeRegistry.isPresent()) {
                        return;
                    }

                    final Member member = atomix.membershipService().getLocalMember();
                    final AtomixBootNodeRegistration registration = new AtomixBootNodeRegistration(serviceId.get(), member);

                    try {
                        nodeRegistry.get().register(registration);
                    } catch (Exception e) {
                        LOGGER.warn("Unable to register this node as service {}", registration, e);
                    }
                }
            );
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            final Member member = atomix.membershipService().getLocalMember();
            final AtomixBootNodeRegistration registration = new AtomixBootNodeRegistration(serviceId.get(), member);

            if (nodeRegistry.isPresent()) {
                nodeRegistry.get().deregister(registration);
            }

            atomix.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return atomix.isRunning();
    }

    public Atomix getNode() {
        return atomix;
    }
}
