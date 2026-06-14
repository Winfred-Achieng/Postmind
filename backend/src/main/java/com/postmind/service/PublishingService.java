package com.postmind.service;

import com.postmind.config.AppProperties;
import com.postmind.dto.PostResponse;
import com.postmind.enums.PostStatus;
import com.postmind.exception.InvalidStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishingService {

    private static final String TWEET_URL = "https://api.twitter.com/2/tweets";

    private final PostService postService;
    private final AppProperties props;

    // Finds all APPROVED posts and publishes them in sequence.
    // Called by SchedulerService on a fixed cadence.
    public void publishApproved() {
        List<PostResponse> approved = postService.getPostsByStatus(PostStatus.APPROVED);
        if (approved.isEmpty()) return;

        log.info("Publishing {} approved post(s)", approved.size());
        approved.forEach(post -> {
            try {
                tweet(post.content());
                postService.updateStatus(post.id(), PostStatus.PUBLISHED);
                log.info("Published post id={}", post.id());
            } catch (Exception e) {
                log.error("Failed to publish post id={}: {}", post.id(), e.getMessage());
            }
        });
    }

    private void tweet(String text) {
        AppProperties.Twitter tw = props.twitter();
        String authHeader = buildOAuth1Header("POST", TWEET_URL, tw);

        RestClient.create(TWEET_URL)
                .post()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(Map.of("text", text))
                .retrieve()
                .toBodilessEntity();
    }

    // ── OAuth 1.0a signing ──────────────────────────────────────────────────

    private String buildOAuth1Header(String method, String url, AppProperties.Twitter tw) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        Map<String, String> oauthParams = new TreeMap<>();
        oauthParams.put("oauth_consumer_key", tw.apiKey());
        oauthParams.put("oauth_nonce", nonce);
        oauthParams.put("oauth_signature_method", "HMAC-SHA1");
        oauthParams.put("oauth_timestamp", timestamp);
        oauthParams.put("oauth_token", tw.accessToken());
        oauthParams.put("oauth_version", "1.0");

        String signature = computeSignature(method, url, oauthParams, tw.apiSecret(), tw.accessTokenSecret());
        oauthParams.put("oauth_signature", signature);

        String headerValue = oauthParams.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=\"" + encode(e.getValue()) + "\"")
                .reduce((a, b) -> a + ", " + b)
                .map(s -> "OAuth " + s)
                .orElseThrow();

        return headerValue;
    }

    private String computeSignature(String method, String url,
                                    Map<String, String> oauthParams,
                                    String apiSecret, String tokenSecret) {
        String paramString = oauthParams.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .reduce((a, b) -> a + "&" + b)
                .orElseThrow();

        String signatureBase = method.toUpperCase()
                + "&" + encode(url)
                + "&" + encode(paramString);

        String signingKey = encode(apiSecret) + "&" + encode(tokenSecret);

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] raw = mac.doFinal(signatureBase.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new InvalidStateException("OAuth 1.0a signing failed: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
