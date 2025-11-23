package com.tradesoncall.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtConfig {
    private String accessTokenSecret;
    private String refreshTokenSecret;
    private Long accessExpirationMs;
    private Long refreshExpirationMs;
}