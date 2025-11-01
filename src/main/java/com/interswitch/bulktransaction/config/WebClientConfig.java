package com.interswitch.bulktransaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${app.downstream.base-url}")
    private String downstreamBaseUrl;


    @Bean
    public WebClient downstreamWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(downstreamBaseUrl)
                .build();
    }

}

