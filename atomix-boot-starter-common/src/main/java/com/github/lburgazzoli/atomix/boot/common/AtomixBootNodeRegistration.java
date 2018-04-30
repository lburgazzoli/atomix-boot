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

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import io.atomix.cluster.Member;
import org.springframework.cloud.client.serviceregistry.Registration;

public class AtomixBootNodeRegistration implements Registration {
    private final String clusterName;
    private final Member member;
    private final Map<String, String> meta;

    public AtomixBootNodeRegistration(String clusterName, Member member) {
        this.clusterName = clusterName;
        this.member = member;
        this.meta = Collections.unmodifiableMap(AtomixBootUtils.getMeta(member));
    }

    @Override
    public String getServiceId() {
        return clusterName;
    }

    @Override
    public String getHost() {
        return member.address().host();
    }

    @Override
    public int getPort() {
        return member.address().port();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.meta;
    }

    @Override
    public String toString() {
        return "AtomixBootNodeRegistration{" +
            "clusterName='" + clusterName + '\'' +
            ", member=" + member +
            ", meta=" + meta +
            '}';
    }
}
