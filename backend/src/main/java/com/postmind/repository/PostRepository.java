package com.postmind.repository;

import com.postmind.entity.Post;
import com.postmind.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByStatusOrderByCreatedAtDesc(PostStatus status);

    List<Post> findAllByOrderByCreatedAtDesc();
}
