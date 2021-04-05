package config;

import io.quarkus.arc.config.ConfigProperties;

import java.util.Optional;

@ConfigProperties
public class QuarkusBotConfig {

    Optional<Boolean> dryRun;

    public void setDryRun(Optional<Boolean> dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isDryRun() {
        return dryRun.isPresent() && dryRun.get();
    }
}
