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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.atomix.cluster.Member;


public final class AtomixUtils {
    public static final String META_MEMBER_ID = "atomix.member.id";
    public static final String META_MEMBER_TYPE = "atomix.member.type";
    public static final String META_MEMBER_ZONE = "atomix.member.zone";
    public static final String META_MEMBER_RACK = "atomix.member.rack";
    public static final String META_MEMBER_HOST = "atomix.member.host";
    public static final String META_MEMBER_CLUSTER_ID = "atomix.member.cluster.id";
    public static final String META_MEMBER_ADDRESS_HOST = "atomix.member.address.host";
    public static final String META_MEMBER_ADDRESS_PORT = "atomix.member.address.port";

    private AtomixUtils() {
    }

    public static Optional<String> getClusterId(Member member) {
        for (String tag : member.tags()) {
            String[] items = tag.split("=");

            if (items.length == 2) {
                if (Objects.equals(META_MEMBER_CLUSTER_ID, items[0])) {
                    return Optional.of(items[1]);
                }
            }
        }

        return Optional.empty();
    }

    public static Map<String, String> getMeta(Member member) {
        final Map<String, String> meta = new HashMap<>();

        putIfNotNull(meta, META_MEMBER_ADDRESS_HOST, member.address().host());
        putIfNotNull(meta, META_MEMBER_ADDRESS_PORT, Integer.toString(member.address().port()));
        putIfNotNull(meta, META_MEMBER_HOST, member.host());
        putIfNotNull(meta, META_MEMBER_RACK, member.rack());
        putIfNotNull(meta, META_MEMBER_ZONE, member.zone());
        putIfNotNull(meta, META_MEMBER_TYPE, member.type().name());
        putIfNotNull(meta, META_MEMBER_ID, member.id().id());

        for (String tag : member.tags()) {
            String[] items = tag.split("=");

            if (items.length == 2) {
                putIfNotNull(meta, items[0], items[1]);
            }
        }

        return meta;
    }

    public static Set<String> getTags(Member member) {
        final Set<String> tags = new HashSet<>();

        for (String tag : member.tags()) {
            String[] items = tag.split("=");

            if (items.length == 1) {
                tags.add(tag);
            }
        }

        return tags;
    }

    public static <K, V> void putIfNotNull(Map<K, V> map, K key, V val) {
        if (map != null && key != null && val != null) {
            map.put(key, val);
        }
    }
}
