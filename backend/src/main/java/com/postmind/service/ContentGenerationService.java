package com.postmind.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.postmind.config.AppProperties;
import com.postmind.entity.Post;
import com.postmind.entity.Trend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentGenerationService {

    private final RestClient perplexityRestClient;
    private final PostService postService;
    private final AppProperties props;

    public Post generateAndSave(Trend trend) {
        String content = callPerplexity(trend.getTitle());
        log.info("Generated post for trend id={}: {}", trend.getId(), content);
        return postService.createDraft(trend, content);
    }

    private String callPerplexity(String trendTitle) {
        String prompt = """
                Write a single engaging Twitter/X post about the following trending topic.
                Rules:
                - Maximum 280 characters
                - Include 1-2 relevant hashtags
                - Be concise, punchy, and informative
                - Output only the tweet text, nothing else

                Trending topic: %s
                """.formatted(trendTitle);

        ChatRequest request = new ChatRequest(
                props.perplexity().model(),
                List.of(
                        new Message("system", "You are a social media expert who writes viral tweets."),
                        new Message("user", prompt)
                )
        );

        ChatResponse response = perplexityRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        if (response == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Empty response from Perplexity API for trend: " + trendTitle);
        }

        return response.choices().getFirst().message().content();
    }

    // Perplexity uses OpenAI-compatible chat format
    private record ChatRequest(
            String model,
            List<Message> messages
    ) {}

    private record Message(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
            @JsonProperty("message") Message message
    ) {}
}
