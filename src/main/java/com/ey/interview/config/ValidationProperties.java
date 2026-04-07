package com.ey.interview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.validation")
public class ValidationProperties {
    private String emailRegex;
    private String passwordRegex;
}
