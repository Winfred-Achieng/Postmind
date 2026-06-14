package com.postmind.controller;

import com.postmind.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trigger")
@RequiredArgsConstructor
public class TriggerController {

    private final SchedulerService schedulerService;

    @PostMapping("/pipeline")
    public ResponseEntity<String> triggerPipeline() {
        schedulerService.runFetchAndGeneratePipeline();
        return ResponseEntity.ok("Pipeline triggered — check /api/posts for new drafts");
    }

    @PostMapping("/publish")
    public ResponseEntity<String> triggerPublish() {
        schedulerService.runPublishingJob();
        return ResponseEntity.ok("Publish job triggered");
    }
}
