package com.github.lburgazzoli.atomix.boot.examples.listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.atomix.cluster.ClusterEvent;
import io.atomix.cluster.ClusterEventListener;
import io.atomix.core.Atomix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientListener implements ClusterEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientListener.class);

    @Autowired
    private Atomix atomix;

    @PostConstruct
    public void initialize() {
        atomix.clusterService().addListener(this);
    }

    @PreDestroy
    public void cleanup() {
        atomix.clusterService().removeListener(this);
    }

    @Override
    public void onEvent(ClusterEvent event) {
        LOGGER.info("onEvent: type={}, subject={}", event.type(), event.subject());
    }
}
