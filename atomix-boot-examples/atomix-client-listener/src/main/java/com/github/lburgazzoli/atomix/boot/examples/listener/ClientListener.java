package com.github.lburgazzoli.atomix.boot.examples.listener;

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
public class ClientListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientListener.class);

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

        LOGGER.info("Joining group '{}' as '{}'", groupName, memberName);
        member = group.join(memberName).join();

        LOGGER.info("Listen join event");
        group.onJoin(m -> LOGGER.info("Member join '{}", m));

        LOGGER.info("Listen leave event");
        group.onLeave(m -> LOGGER.info("Member left '{}'", m));
    }

    @PreDestroy
    public void cleanup() {
        if (member != null) {
            member.leave().join();
        }
    }
}
