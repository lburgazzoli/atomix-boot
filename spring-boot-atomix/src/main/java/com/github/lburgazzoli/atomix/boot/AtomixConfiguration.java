package com.github.lburgazzoli.atomix.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atomix")
public class AtomixConfiguration {
    /**
     * enable/disable.
     */

    private boolean enabled = true;
    /**
     * the configuration path
     */
    private String configurationFile;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }
}
