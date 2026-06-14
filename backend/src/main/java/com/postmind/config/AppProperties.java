package com.postmind.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "postmind")
public record AppProperties(HackerNews hackernews, Perplexity perplexity, Twitter twitter) {

    public record HackerNews(
            String baseUrl,
            int fetchLimit,
            int minScore
    ) {}

    public record Perplexity(
            String baseUrl,
            String apiKey,
            String model
    ) {}

    public record Twitter(
            String baseUrl,
            String apiKey,
            String apiSecret,
            String accessToken,
            String accessTokenSecret
    ) {}
}
