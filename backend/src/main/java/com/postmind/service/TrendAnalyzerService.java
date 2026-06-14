package com.postmind.service;

import com.postmind.entity.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class TrendAnalyzerService {

    private static final int TOP_N = 5;

    /**
     * Ranks trends by score and returns the top N candidates for post generation.
     * Called by SchedulerService after a fetch cycle.
     */
    public List<Trend> selectTopCandidates(List<Trend> trends) {
        List<Trend> top = trends.stream()
                .sorted(Comparator.comparingInt(Trend::getScore).reversed())
                .limit(TOP_N)
                .toList();

        log.info("Analyzed {} trends, selected {} top candidates", trends.size(), top.size());
        return top;
    }
}
