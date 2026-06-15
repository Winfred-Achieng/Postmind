package com.postmind.service;

import com.postmind.entity.Post;
import com.postmind.entity.Trend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final TrendFetchService trendFetchService;
    private final TrendAnalyzerService trendAnalyzerService;
    private final ContentGenerationService contentGenerationService;
    private final PublishingService publishingService;
    private final TelegramNotificationService telegramNotificationService;

    @Scheduled(cron = "0 0 * * * *")
    public void runFetchAndGeneratePipeline() {
        log.info("Starting fetch-and-generate pipeline");
        try {
            List<Trend> fetched = trendFetchService.fetchAndPersist();
            List<Trend> candidates = trendAnalyzerService.selectTopCandidates(fetched);

            List<Post> drafts = new ArrayList<>();
            candidates.forEach(trend -> {
                try {
                    Post post = contentGenerationService.generateAndSave(trend);
                    drafts.add(post);
                } catch (Exception e) {
                    log.error("Content generation failed for trend id={}: {}", trend.getId(), e.getMessage());
                }
            });

            log.info("Pipeline complete — {} drafts created", drafts.size());

            if (!drafts.isEmpty()) {
                telegramNotificationService.sendDraftSummary(drafts);
            }
        } catch (Exception e) {
            log.error("Fetch-and-generate pipeline failed: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 300_000)
    public void runPublishingJob() {
        log.info("Starting publishing job");
        try {
            publishingService.publishApproved();
        } catch (Exception e) {
            log.error("Publishing job failed: {}", e.getMessage());
        }
    }
}
