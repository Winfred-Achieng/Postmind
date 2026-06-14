package com.postmind.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class RestClientConfig {

    @Bean
    RestClient hackerNewsRestClient(AppProperties props) {
        return RestClient.builder()
                .baseUrl(props.hackernews().baseUrl())
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    RestClient perplexityRestClient(AppProperties props) {
        return RestClient.builder()
                .baseUrl(props.perplexity().baseUrl())
                .defaultHeader("Authorization", "Bearer " + props.perplexity().apiKey())
                .defaultHeader("content-type", "application/json")
                .build();
    }
}
