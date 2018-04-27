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

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

public abstract class AbstractAtomixBootNodeRegistry<R extends Registration> implements AtomixBootNodeRegistry {
    private final ServiceRegistry<R> serviceRegistry;

    public AbstractAtomixBootNodeRegistry(ServiceRegistry<R> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void register(AtomixBootNodeRegistration registration) {
        this.serviceRegistry.register(toNativeRegistration(registration));
    }

    @Override
    public void deregister(AtomixBootNodeRegistration registration) {
        this.serviceRegistry.deregister(toNativeRegistration(registration));
    }

    protected abstract R toNativeRegistration(AtomixBootNodeRegistration registration);
}
