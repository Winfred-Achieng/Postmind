package com.postmind.dto;

import com.postmind.entity.Trend;
import com.postmind.enums.TrendSource;

import java.time.LocalDateTime;

public record TrendResponse(
        Long id,
        String title,
        TrendSource source,
        Integer score,
        LocalDateTime createdAt
) {
    public static TrendResponse from(Trend trend) {
        return new TrendResponse(
                trend.getId(),
                trend.getTitle(),
                trend.getSource(),
                trend.getScore(),
                trend.getCreatedAt()
        );
    }
}
