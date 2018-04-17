package com.github.lburgazzoli.atomix.boot.examples.leadership;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.atomix.core.Atomix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientLeadershipListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientLeadershipListener.class);

    @Autowired
    private Atomix atomix;

    @PostConstruct
    public void initialize() {
        // TBD
    }

    @PreDestroy
    public void cleanup() {
        // TBD
    }
}
