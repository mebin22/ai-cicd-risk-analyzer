package com.mabin.riskanalyzer.service;

import com.mabin.riskanalyzer.dto.RiskRequestDTO;
import com.mabin.riskanalyzer.dto.RiskResponseDTO;
import com.mabin.riskanalyzer.model.RiskAnalysis;
import com.mabin.riskanalyzer.repository.RiskAnalysisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RiskAnalysisService {

    private final RiskAnalysisRepository repository;
    private final RiskScoringService riskScoringService;

    public RiskAnalysisService(RiskAnalysisRepository repository,
                               RiskScoringService riskScoringService) {
        this.repository = repository;
        this.riskScoringService = riskScoringService;
    }

    public RiskResponseDTO analyzeRisk(RiskRequestDTO request) {

        String logs = request.getLogs().toLowerCase();

        RiskResponseDTO response = new RiskResponseDTO();

        int riskScore = riskScoringService.calculateRiskScore(logs);

        response.setRiskScore(riskScore);
        response.setRiskLevel(riskScoringService.getRiskLevel(riskScore));
        response.setDeploymentDecision(riskScoringService.getDeploymentDecision(riskScore));

        if (logs.contains("unit test") || logs.contains("test failed") || logs.contains("assertion failed")) {
            response.setFailureCause("Unit test failure detected");
            response.setRecommendation("Fix failing test cases before deployment");

        } else if (logs.contains("docker") && (logs.contains("error") || logs.contains("failed"))) {
            response.setFailureCause("Docker build issue detected");
            response.setRecommendation("Check Dockerfile, build context, and dependency installation steps");

        } else if (logs.contains("dependency") || logs.contains("version conflict")) {
            response.setFailureCause("Dependency issue detected");
            response.setRecommendation("Review dependency versions and update project configuration");

        } else if (logs.contains("deployment failed") || logs.contains("connection refused")) {
            response.setFailureCause("Deployment failure detected");
            response.setRecommendation("Check deployment environment, service availability, and configuration");

        } else if (logs.contains("memory") || logs.contains("timeout") || logs.contains("service unavailable")) {
            response.setFailureCause("Infrastructure or runtime issue detected");
            response.setRecommendation("Check server resources, timeout settings, and service availability");

        } else if (logs.contains("success") || logs.contains("passed")) {
            response.setFailureCause("No major issue detected");
            response.setRecommendation("Deployment can proceed");

        } else {
            response.setFailureCause("Unclear pipeline condition");
            response.setRecommendation("Review logs manually before deployment");
        }

        RiskAnalysis analysis = new RiskAnalysis();
        analysis.setLogs(request.getLogs());
        analysis.setRiskScore(response.getRiskScore());
        analysis.setRiskLevel(response.getRiskLevel());
        analysis.setFailureCause(response.getFailureCause());
        analysis.setRecommendation(response.getRecommendation());
        analysis.setDeploymentDecision(response.getDeploymentDecision());
        analysis.setTimestamp(LocalDateTime.now());

        repository.save(analysis);

        return response;
    }
}