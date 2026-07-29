package com.mabin.riskanalyzer.repository;

import com.mabin.riskanalyzer.model.RiskAnalysis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RiskAnalysisRepository
        extends JpaRepository<RiskAnalysis, Long> {

    long countByRiskLevel(String riskLevel);

    @Query("SELECT AVG(r.riskScore) FROM RiskAnalysis r")
    Double getAverageRiskScore();

    List<RiskAnalysis> findAllByOrderByTimestampDesc(
            Pageable pageable
    );

    boolean existsByGithubRunId(Long githubRunId);

    @Query(
            value = """
                SELECT COUNT(*)
                FROM risk_analysis
                WHERE UPPER(TRIM(deployment_decision))
                      = UPPER(TRIM(:decision))
                """,
            nativeQuery = true
    )
    long countByDecision(
            @Param("decision") String decision
    );

    @Query("""
           SELECT AVG(r.confidence)
           FROM RiskAnalysis r
           WHERE r.confidence IS NOT NULL
           """)
    Double getAverageConfidence();
}