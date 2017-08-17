package com.github.lburgazzoli.atomix.boot.examples.leadership;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.atomix.AtomixClient;
import io.atomix.group.DistributedGroup;
import io.atomix.group.LocalMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClientLeadershipListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientLeadershipListener.class);

    @Autowired
    private AtomixClient client;
    @Value("${pod.namespace}")
    private String groupName;
    @Value("${pod.name}")
    private String memberName;

    private LocalMember member;

    @PostConstruct
    public void initialize() {
        LOGGER.info("Getting group '{}'", groupName);
        DistributedGroup group = client.getGroup(groupName).join();

        LOGGER.info("Listen election event");
        group.election().onElection(t -> LOGGER.info("Member election '{}", t.leader()));

        LOGGER.info("Joining group '{}' as '{}'", groupName, memberName);
        member = group.join(memberName).join();
    }

    @PreDestroy
    public void cleanup() {
        if (member != null) {
            member.leave().join();
        }
    }
}
