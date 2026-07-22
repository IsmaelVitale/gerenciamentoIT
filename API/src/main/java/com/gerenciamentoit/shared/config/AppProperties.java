package com.gerenciamentoit.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

public final class AppProperties {

    private AppProperties() {
    }

    @ConfigurationProperties(prefix = "app.security")
    public record Security(Duration sessionDuration) {
        public Security {
            if (sessionDuration == null || sessionDuration.isZero() || sessionDuration.isNegative()) {
                sessionDuration = Duration.ofHours(8);
            }
        }
    }

    @ConfigurationProperties(prefix = "app.cors")
    public record Cors(List<String> allowedOrigins) {
        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        }
    }

    @ConfigurationProperties(prefix = "app.bootstrap")
    public record Bootstrap(boolean enabled, String adminMatricula, String adminNome) {
    }
}
