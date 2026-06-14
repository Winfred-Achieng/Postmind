package com.postmind.repository;

import com.postmind.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    Optional<Approval> findByPostId(Long postId);
}
