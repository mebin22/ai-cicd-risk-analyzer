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

    public RiskAnalysisService(RiskAnalysisRepository repository) {
        this.repository = repository;
    }

    public RiskResponseDTO analyzeRisk(RiskRequestDTO request) {

        String logs = request.getLogs().toLowerCase();

        RiskResponseDTO response = new RiskResponseDTO();

        if (logs.contains("unit test") || logs.contains("test failed")) {
            response.setRiskScore(85);
            response.setRiskLevel("HIGH");
            response.setFailureCause("Unit test failure detected");
            response.setRecommendation("Fix failing test cases before deployment");
            response.setDeploymentDecision("STOP");

        } else if (logs.contains("docker") && logs.contains("error")) {
            response.setRiskScore(90);
            response.setRiskLevel("HIGH");
            response.setFailureCause("Docker build error detected");
            response.setRecommendation("Check Dockerfile and dependency installation steps");
            response.setDeploymentDecision("STOP");

        } else if (logs.contains("dependency") || logs.contains("version conflict")) {
            response.setRiskScore(65);
            response.setRiskLevel("MEDIUM");
            response.setFailureCause("Dependency issue detected");
            response.setRecommendation("Review dependency versions and update configuration");
            response.setDeploymentDecision("REVIEW");

        } else if (logs.contains("deployment failed")) {
            response.setRiskScore(88);
            response.setRiskLevel("HIGH");
            response.setFailureCause("Deployment failure detected");
            response.setRecommendation("Check deployment environment and service configuration");
            response.setDeploymentDecision("STOP");

        } else if (logs.contains("success") || logs.contains("passed")) {
            response.setRiskScore(15);
            response.setRiskLevel("LOW");
            response.setFailureCause("No major issue detected");
            response.setRecommendation("Deployment can proceed");
            response.setDeploymentDecision("PROCEED");

        } else {
            response.setRiskScore(50);
            response.setRiskLevel("MEDIUM");
            response.setFailureCause("Unclear pipeline condition");
            response.setRecommendation("Review logs manually before deployment");
            response.setDeploymentDecision("REVIEW");
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