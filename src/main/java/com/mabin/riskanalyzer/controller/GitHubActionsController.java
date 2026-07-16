package com.mabin.riskanalyzer.controller;

import com.mabin.riskanalyzer.dto.GitHubAnalysisResponseDTO;
import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
import com.mabin.riskanalyzer.dto.ModelMetricsDTO;
import com.mabin.riskanalyzer.model.RiskAnalysis;
import com.mabin.riskanalyzer.service.GitHubActionsService;
import com.mabin.riskanalyzer.service.MlRiskService;
import com.mabin.riskanalyzer.service.RiskAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubActionsController {

    private final GitHubActionsService gitHubActionsService;
    private final MlRiskService mlRiskService;
    private final RiskAnalysisService riskAnalysisService;

    public GitHubActionsController(
            GitHubActionsService gitHubActionsService, MlRiskService mlRiskService, RiskAnalysisService riskAnalysisService
    ) {
        this.gitHubActionsService = gitHubActionsService;
        this.mlRiskService = mlRiskService;
        this.riskAnalysisService = riskAnalysisService;
    }

    @GetMapping("/api/github/latest-run")
    public String latestRun() {
        return gitHubActionsService.getLatestWorkflowRuns();
    }

    @GetMapping("/api/github/logs")
    public String logs() {
        return gitHubActionsService.getLatestJobLogs();
    }

    @GetMapping("/api/github/run-id")
    public Long runId() {
        return gitHubActionsService.getLatestRunId();
    }

    @GetMapping("/api/github/summary")
    public String summary() {
        return gitHubActionsService.buildWorkflowSummary();
    }

    @PostMapping("/api/github/analyze-latest")
    public GitHubAnalysisResponseDTO analyzeLatest() {
        String logs = gitHubActionsService.buildWorkflowSummary();

        MlRiskResponseDTO mlResult = mlRiskService.predictRisk(logs);

        RiskAnalysis saved = riskAnalysisService.saveMlAnalysis(logs, mlResult);

        GitHubAnalysisResponseDTO response = new GitHubAnalysisResponseDTO();
        response.setId(saved.getId());
        response.setSource("GitHub Actions");
        response.setRiskLevel(saved.getRiskLevel());
        response.setRiskScore(saved.getRiskScore());
        response.setConfidence(saved.getConfidence());
        response.setDeploymentDecision(saved.getDeploymentDecision());
        response.setRecommendation(saved.getRecommendation());

        return response;
    }

    @GetMapping("/api/ml/metrics")
    public ModelMetricsDTO getMetrics() {
        return mlRiskService.getMetrics();
    }
}