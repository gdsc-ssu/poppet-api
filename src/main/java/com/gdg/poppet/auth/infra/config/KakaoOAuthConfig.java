package com.gdg.poppet.auth.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kakao.oauth2")
@Getter
@Setter
public class KakaoOAuthConfig {
    private String tokenUri;
    private String userInfoUri;
    private String client;
    private String redirect;
}
