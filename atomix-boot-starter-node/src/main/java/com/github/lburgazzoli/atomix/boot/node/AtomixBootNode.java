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
import java.util.concurrent.atomic.AtomicReference;

import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.Lifecycle;

public final class AtomixBootNode implements Lifecycle {
    private final AtomicBoolean running;
    private final Atomix atomix;
    private final Optional<String> serviceId;
    private final Optional<ServiceRegistry> serviceRegistry;
    private final AtomicReference<Registration> serviceRegistration;

    public AtomixBootNode(Atomix atomix, Optional<String> serviceId, Optional<ServiceRegistry> serviceRegistry) {
        this.running = new AtomicBoolean(false);
        this.atomix = atomix;
        this.serviceId = serviceId;
        this.serviceRegistry = serviceRegistry;
        this.serviceRegistration = new AtomicReference<>();
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            atomix.start().thenAccept(
                a -> {
                    if (!serviceId.isPresent() || !serviceRegistry.isPresent()) {
                        return;
                    }

                    final Node node = atomix.clusterService().getLocalNode();
                    final Registration registration = new AtomixBootNodeRegistration(serviceId.get(), node);

                    serviceRegistration.set(registration);
                    serviceRegistry.get().register(registration);
                }
            );
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            final Registration registration = serviceRegistration.get();

            if (registration != null && serviceRegistry.isPresent()) {
                serviceRegistry.get().deregister(registration);
            }

            atomix.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return atomix.isRunning();
    }

    public Atomix node() {
        return atomix;
    }
}
