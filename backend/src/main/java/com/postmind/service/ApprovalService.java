package com.postmind.service;

import com.postmind.dto.ApprovalResponse;
import com.postmind.entity.Approval;
import com.postmind.entity.Post;
import com.postmind.enums.ApprovalDecision;
import com.postmind.enums.PostStatus;
import com.postmind.exception.InvalidStateException;
import com.postmind.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final PostService postService;

    @Transactional
    public ApprovalResponse decide(Long postId, ApprovalDecision decision) {
        Post post = postService.findOrThrow(postId);

        guardAlreadyDecided(postId, post);

        PostStatus newStatus = decision == ApprovalDecision.APPROVED
                ? PostStatus.APPROVED
                : PostStatus.REJECTED;

        postService.updateStatus(postId, newStatus);

        Approval approval = new Approval();
        approval.setPost(post);
        approval.setDecision(decision);

        return ApprovalResponse.from(approvalRepository.save(approval));
    }

    @Transactional(readOnly = true)
    public ApprovalResponse getByPostId(Long postId) {
        return approvalRepository.findByPostId(postId)
                .map(ApprovalResponse::from)
                .orElseThrow(() -> new InvalidStateException("No approval decision found for post: " + postId));
    }

    private void guardAlreadyDecided(Long postId, Post post) {
        if (post.getStatus() != PostStatus.DRAFT) {
            throw new InvalidStateException(
                    "Post " + postId + " cannot be reviewed — current status: " + post.getStatus()
            );
        }
    }
}
