package com.postmind.controller;

import com.postmind.dto.TrendResponse;
import com.postmind.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
public class TrendController {

    private final TrendService trendService;

    @GetMapping
    public ResponseEntity<List<TrendResponse>> getAll() {
        return ResponseEntity.ok(trendService.getAllTrends());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrendResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(trendService.getTrendById(id));
    }
}
