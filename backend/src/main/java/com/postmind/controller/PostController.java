package com.postmind.controller;

import com.postmind.dto.PostResponse;
import com.postmind.enums.PostStatus;
import com.postmind.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAll(
            @RequestParam(required = false) PostStatus status
    ) {
        List<PostResponse> posts = status != null
                ? postService.getPostsByStatus(status)
                : postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }
}
