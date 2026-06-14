package com.postmind.service;

import com.postmind.dto.TrendResponse;
import com.postmind.entity.Trend;
import com.postmind.enums.TrendSource;
import com.postmind.exception.ResourceNotFoundException;
import com.postmind.repository.TrendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final TrendRepository trendRepository;

    @Transactional(readOnly = true)
    public List<TrendResponse> getAllTrends() {
        return trendRepository.findAll().stream()
                .map(TrendResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrendResponse getTrendById(Long id) {
        return TrendResponse.from(findOrThrow(id));
    }

    // Called internally by TrendFetchService (Step 5) — not exposed via REST
    @Transactional
    public Trend save(String title, TrendSource source, int score) {
        Trend trend = new Trend();
        trend.setTitle(title);
        trend.setSource(source);
        trend.setScore(score);
        return trendRepository.save(trend);
    }

    Trend findOrThrow(Long id) {
        return trendRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trend", id));
    }
}
