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

import java.util.List;

import io.atomix.core.Atomix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

public final class AtomixBoot implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomixBoot.class);

    private final Atomix atomix;
    private final List<Listener> listeners;

    public AtomixBoot(Atomix atomix, List<Listener> listeners) {
         this.atomix = atomix;
        this.listeners = listeners;
    }

    @Override
    public void start() {
        if (!atomix.isRunning()) {
            atomix.start().thenAccept(
                a -> {
                    LOGGER.info("Node {} started", atomix.membershipService().getLocalMember().id());

                    for (Listener listener: listeners) {
                        listener.started(atomix);
                    }

                    /*
                    if (!serviceId.isPresent() || !nodeRegistry.isPresent()) {
                        return;
                    }

                    //atomix.membershipService().getLocalMember().
                    final Member member = atomix.membershipService().getLocalMember();
                    final AtomixBootRegistration registration = new AtomixBootNodeRegistration(serviceId.get(), member);

                    try {
                        nodeRegistry.get().register(registration);
                    } catch (Exception e) {
                        LOGGER.warn("Unable to register this node as service {}", registration, e);
                    }
                    */
                }
            );
        }
    }

    @Override
    public void stop() {
        atomix.stop().thenAccept(
            a -> {
                LOGGER.info("Node {} stopped", atomix.membershipService().getLocalMember().id());

                for (Listener listener: listeners) {
                    listener.stopped(atomix);
                }
            }
        );

            /*

        if (running.compareAndSet(true, false)) {
            final Member member = atomix.membershipService().getLocalMember();
            final AtomixBootNodeRegistration registration = new AtomixBootNodeRegistration(serviceId.get(), member);

            if (nodeRegistry.isPresent()) {
                nodeRegistry.get().deregister(registration);
            }

            atomix.stop();
        }
        */
    }

    @Override
    public boolean isRunning() {
        return atomix.isRunning();
    }

    public Atomix getNode() {
        return atomix;
    }

    // ****************
    //
    // ****************

    public interface Listener {
        void started(Atomix atomix);

        void stopped(Atomix atomix);
    }
}
