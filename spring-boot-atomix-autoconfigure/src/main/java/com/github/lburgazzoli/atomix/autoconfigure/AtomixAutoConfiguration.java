package com.github.lburgazzoli.atomix.autoconfigure;

import java.util.List;

import com.github.lburgazzoli.atomix.boot.AtomixInstance;
import io.atomix.core.Atomix;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "atomix", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AtomixConfiguration.class)
public class AtomixAutoConfiguration {

    @Bean(name = "atomix-instance", initMethod = "start", destroyMethod = "stop")
    public AtomixInstance atomix(
            AtomixConfiguration configuration,
            List<AtomixInstance.Listener> listeners) {

        return new AtomixInstance(
            Atomix.builder(configuration.getConfigurationFile()).build(),
            listeners
        );
    }
}
