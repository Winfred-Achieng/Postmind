package com.postmind.dto;

import com.postmind.enums.ApprovalDecision;
import jakarta.validation.constraints.NotNull;

public record ApprovalRequest(
        @NotNull ApprovalDecision decision
) {
}
