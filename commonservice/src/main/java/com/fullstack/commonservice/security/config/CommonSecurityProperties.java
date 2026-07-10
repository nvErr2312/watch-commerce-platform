package com.fullstack.commonservice.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class CommonSecurityProperties {
    private String jwtSecret;
    private String internalToken;
}
