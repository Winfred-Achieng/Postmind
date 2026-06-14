package com.postmind.repository;

import com.postmind.entity.Trend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendRepository extends JpaRepository<Trend, Long> {
}
