package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {
}
