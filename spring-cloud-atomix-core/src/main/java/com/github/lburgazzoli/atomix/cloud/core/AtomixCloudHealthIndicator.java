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
package com.github.lburgazzoli.atomix.cloud.core;

import java.util.Objects;

import com.github.lburgazzoli.atomix.boot.AtomixBoot;
import io.atomix.cluster.Member;
import io.atomix.core.election.Leadership;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

public class AtomixCloudHealthIndicator extends AbstractHealthIndicator {

    private AtomixBoot atomix;

    public AtomixCloudHealthIndicator(AtomixBoot atomix) {
        this.atomix = atomix;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            final Member local = atomix.getNode().membershipService().getLocalMember();
            final Leadership<Object> leader = atomix.getNode().getLeaderElection("").getLeadership();

            if (atomix.isRunning()) {
                builder.up().withDetail("leader", Objects.equals(leader.leader(), local.id()));
            } else {
                builder.down();
            }
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
