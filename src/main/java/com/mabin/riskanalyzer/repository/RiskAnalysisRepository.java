package com.mabin.riskanalyzer.repository;

import com.mabin.riskanalyzer.model.RiskAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAnalysisRepository extends JpaRepository<RiskAnalysis, Long> {
}