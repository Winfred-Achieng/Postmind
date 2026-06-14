package com.postmind.dto;

import com.postmind.entity.Post;
import com.postmind.enums.PostStatus;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long trendId,
        String trendTitle,
        String content,
        PostStatus status,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTrend().getId(),
                post.getTrend().getTitle(),
                post.getContent(),
                post.getStatus(),
                post.getCreatedAt()
        );
    }
}
