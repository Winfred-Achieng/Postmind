package com.postmind.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.postmind.config.AppProperties;
import com.postmind.enums.ApprovalDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramCallbackService {

    private final AppProperties props;
    private final ApprovalService approvalService;
    private final TelegramNotificationService telegramNotificationService;

    private final AtomicLong offset = new AtomicLong(0);

    @Scheduled(fixedDelay = 3000)
    public void pollUpdates() {
        try {
            AppProperties.Telegram tg = props.telegram();
            UpdatesResponse response = RestClient.create("https://api.telegram.org")
                .post()
                .uri("/bot{token}/getUpdates", tg.botToken())
                .body(Map.of(
                    "offset", offset.get(),
                    "timeout", 0,
                    "allowed_updates", List.of("callback_query")
                ))
                .retrieve()
                .body(UpdatesResponse.class);

            if (response == null || !response.ok() || response.result().isEmpty()) return;

            for (Update update : response.result()) {
                offset.set(update.updateId() + 1);
                if (update.callbackQuery() != null) {
                    handleCallback(update.callbackQuery());
                }
            }
        } catch (Exception e) {
            log.warn("Telegram poll error: {}", e.getMessage());
        }
    }

    private void handleCallback(CallbackQuery cq) {
        String data = cq.data();

        if ("noop".equals(data)) {
            telegramNotificationService.answerCallback(cq.id(), "Already decided.");
            return;
        }

        String[] parts = data.split(":");
        if (parts.length != 2) return;

        String action = parts[0];
        Long postId;
        try {
            postId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }

        boolean approving = "approve".equals(action);
        ApprovalDecision decision = approving ? ApprovalDecision.APPROVED : ApprovalDecision.REJECTED;

        String toast;
        try {
            approvalService.decide(postId, decision);
            toast = approving ? "✅ Approved — will publish within 5 minutes." : "❌ Rejected.";
            telegramNotificationService.markDecided(postId, approving);
        } catch (Exception e) {
            toast = "Already decided: " + e.getMessage();
        }

        telegramNotificationService.answerCallback(cq.id(), toast);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record UpdatesResponse(boolean ok, List<Update> result) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Update(
        @JsonProperty("update_id") long updateId,
        @JsonProperty("callback_query") CallbackQuery callbackQuery
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CallbackQuery(String id, String data) {}
}
