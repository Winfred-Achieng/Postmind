package com.postmind.controller;

import com.postmind.dto.ApprovalRequest;
import com.postmind.dto.ApprovalResponse;
import com.postmind.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalResponse> decide(
            @PathVariable Long postId,
            @Valid @RequestBody ApprovalRequest request
    ) {
        return ResponseEntity.ok(approvalService.decide(postId, request.decision()));
    }

    @GetMapping
    public ResponseEntity<ApprovalResponse> getByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(approvalService.getByPostId(postId));
    }
}
