package com.postmind.service;

import com.postmind.dto.PostResponse;
import com.postmind.entity.Post;
import com.postmind.entity.Trend;
import com.postmind.enums.PostStatus;
import com.postmind.exception.ResourceNotFoundException;
import com.postmind.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByStatus(PostStatus status) {
        return postRepository.findAllByStatusOrderByCreatedAtDesc(status).stream()
                .map(PostResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        return PostResponse.from(findOrThrow(id));
    }

    // Called internally by ContentGenerationService (Step 5) — not exposed via REST
    @Transactional
    public Post createDraft(Trend trend, String content) {
        Post post = new Post();
        post.setTrend(trend);
        post.setContent(content);
        // status defaults to DRAFT via @PrePersist
        return postRepository.save(post);
    }

    // Package-private: only ApprovalService and PublishingService drive status transitions
    @Transactional
    Post updateStatus(Long postId, PostStatus newStatus) {
        Post post = findOrThrow(postId);
        post.setStatus(newStatus);
        return postRepository.save(post);
    }

    Post findOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
    }
}
