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

import java.util.HashMap;
import java.util.Map;

import io.atomix.cluster.Node;

public final class AtomixBootUtils {
    private AtomixBootUtils() {
    }

    public static  Map<String, String>  getMeta(Node node) {
        Map<String, String> meta = new HashMap<>();
        meta.put("atomix.node.address.host", node.address().host());
        meta.put("atomix.node.address.port", Integer.toString(node.address().port()));
        meta.put("atomix.node.host", node.host());
        meta.put("atomix.node.rack", node.rack());
        meta.put("atomix.node.zone", node.zone());
        meta.put("atomix.node.type", node.type().name());
        meta.put("atomix.node.id", node.id().id());

        return meta;
    }
}
