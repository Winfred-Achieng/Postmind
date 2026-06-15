package com.postmind.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.postmind.config.AppProperties;
import com.postmind.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final AppProperties props;

    private volatile Long summaryMessageId = null;
    private final List<Long> currentPostIds = new ArrayList<>();
    private final Map<Long, Boolean> decisions = new ConcurrentHashMap<>();

    public synchronized void sendDraftSummary(List<Post> posts) {
        currentPostIds.clear();
        decisions.clear();
        posts.forEach(p -> currentPostIds.add(p.getId()));

        AppProperties.Telegram tg = props.telegram();
        String text = buildText(posts);
        List<List<Map<String, Object>>> keyboard = buildKeyboard();

        if (summaryMessageId != null) {
            try {
                RestClient.create("https://api.telegram.org")
                    .post()
                    .uri("/bot{token}/editMessageText", tg.botToken())
                    .body(Map.of(
                        "chat_id", tg.chatId(),
                        "message_id", summaryMessageId,
                        "text", text,
                        "reply_markup", Map.of("inline_keyboard", keyboard)
                    ))
                    .retrieve()
                    .toBodilessEntity();
                return;
            } catch (Exception e) {
                log.warn("Could not edit previous Telegram message, sending new one");
                summaryMessageId = null;
            }
        }

        try {
            SendMessageResponse response = RestClient.create("https://api.telegram.org")
                .post()
                .uri("/bot{token}/sendMessage", tg.botToken())
                .body(Map.of(
                    "chat_id", tg.chatId(),
                    "text", text,
                    "reply_markup", Map.of("inline_keyboard", keyboard)
                ))
                .retrieve()
                .body(SendMessageResponse.class);
            if (response != null && response.ok()) {
                summaryMessageId = response.result().messageId();
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram draft summary: {}", e.getMessage());
        }
    }

    public synchronized void markDecided(Long postId, boolean approved) {
        decisions.put(postId, approved);
        if (summaryMessageId == null) return;

        AppProperties.Telegram tg = props.telegram();
        try {
            RestClient.create("https://api.telegram.org")
                .post()
                .uri("/bot{token}/editMessageReplyMarkup", tg.botToken())
                .body(Map.of(
                    "chat_id", tg.chatId(),
                    "message_id", summaryMessageId,
                    "reply_markup", Map.of("inline_keyboard", buildKeyboard())
                ))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to update Telegram keyboard after decision: {}", e.getMessage());
        }
    }

    public void answerCallback(String callbackQueryId, String text) {
        try {
            AppProperties.Telegram tg = props.telegram();
            RestClient.create("https://api.telegram.org")
                .post()
                .uri("/bot{token}/answerCallbackQuery", tg.botToken())
                .body(Map.of("callback_query_id", callbackQueryId, "text", text))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to answer Telegram callback: {}", e.getMessage());
        }
    }

    public void sendMessage(String text) {
        try {
            AppProperties.Telegram tg = props.telegram();
            RestClient.create("https://api.telegram.org")
                .post()
                .uri("/bot{token}/sendMessage", tg.botToken())
                .body(Map.of("chat_id", tg.chatId(), "text", text))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send Telegram notification: {}", e.getMessage());
        }
    }

    private String buildText(List<Post> posts) {
        StringBuilder sb = new StringBuilder("📝 New drafts ready — tap to approve or reject:\n\n");
        int i = 1;
        for (Post post : posts) {
            sb.append(i++).append(". ").append(post.getContent()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private List<List<Map<String, Object>>> buildKeyboard() {
        List<Map<String, Object>> approveRow = new ArrayList<>();
        List<Map<String, Object>> rejectRow = new ArrayList<>();
        List<List<Map<String, Object>>> decidedRows = new ArrayList<>();

        int i = 1;
        for (Long postId : currentPostIds) {
            if (decisions.containsKey(postId)) {
                boolean approved = decisions.get(postId);
                String label = approved ? "✅ " + i + " — Approved" : "❌ " + i + " — Rejected";
                decidedRows.add(List.of(Map.of("text", label, "callback_data", "noop")));
            } else {
                approveRow.add(Map.of("text", "✅ " + i, "callback_data", "approve:" + postId));
                rejectRow.add(Map.of("text", "❌ " + i, "callback_data", "reject:" + postId));
            }
            i++;
        }

        List<List<Map<String, Object>>> keyboard = new ArrayList<>();
        if (!approveRow.isEmpty()) keyboard.add(approveRow);
        if (!rejectRow.isEmpty()) keyboard.add(rejectRow);
        keyboard.addAll(decidedRows);
        return keyboard;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SendMessageResponse(boolean ok, TgMessage result) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TgMessage(@JsonProperty("message_id") Long messageId) {}
}
