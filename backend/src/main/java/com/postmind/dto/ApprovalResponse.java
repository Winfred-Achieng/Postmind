package com.postmind.dto;

import com.postmind.entity.Approval;
import com.postmind.enums.ApprovalDecision;

import java.time.LocalDateTime;

public record ApprovalResponse(
        Long id,
        Long postId,
        ApprovalDecision decision,
        LocalDateTime timestamp
) {
    public static ApprovalResponse from(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getPost().getId(),
                approval.getDecision(),
                approval.getTimestamp()
        );
    }
}
