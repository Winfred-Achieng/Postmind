package com.postmind.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postmind.config.AppProperties;
import com.postmind.entity.Trend;
import com.postmind.enums.TrendSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendFetchService {

    private final RestClient hackerNewsRestClient;
    private final TrendService trendService;
    private final AppProperties props;

    public List<Trend> fetchAndPersist() {
        try {
            Integer[] ids = hackerNewsRestClient.get()
                    .uri("/topstories.json")
                    .retrieve()
                    .body(Integer[].class);

            if (ids == null || ids.length == 0) return Collections.emptyList();

            List<Trend> trends = Arrays.stream(ids)
                    .limit(props.hackernews().fetchLimit())
                    .map(this::fetchItem)
                    .filter(Objects::nonNull)
                    .filter(item -> item.isStory() && item.score() >= props.hackernews().minScore())
                    .map(item -> trendService.save(item.title(), TrendSource.HACKERNEWS, item.score()))
                    .toList();

            log.info("Fetched {} trends from HackerNews", trends.size());
            return trends;

        } catch (Exception e) {
            log.error("Failed to fetch HackerNews trends: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private HnItem fetchItem(int id) {
        try {
            return hackerNewsRestClient.get()
                    .uri("/item/{id}.json", id)
                    .retrieve()
                    .body(HnItem.class);
        } catch (Exception e) {
            log.warn("Failed to fetch HN item {}: {}", id, e.getMessage());
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record HnItem(String title, int score, String type) {
        boolean isStory() { return "story".equals(type); }
    }
}
