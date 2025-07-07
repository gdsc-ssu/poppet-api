package com.gdg.poppet.auth.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "apple.oauth")
@Getter
@Setter
public class AppleOAuthConfig {
    private String teamId;
    private String clientId;
    private String keyId;
    private String privateKeyPath;
    private String redirectUri;
}
