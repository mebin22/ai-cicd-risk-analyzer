package com.mabin.riskanalyzer.repository;

import com.mabin.riskanalyzer.model.RiskAnalysis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RiskAnalysisRepository extends JpaRepository<RiskAnalysis, Long> {

    long countByRiskLevel(String riskLevel);

    @Query("SELECT AVG(r.riskScore) FROM RiskAnalysis r")
    Double getAverageRiskScore();

    List<RiskAnalysis> findAllByOrderByTimestampDesc(Pageable pageable);
}