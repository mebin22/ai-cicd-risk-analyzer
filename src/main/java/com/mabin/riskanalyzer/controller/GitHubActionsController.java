package com.mabin.riskanalyzer.controller;

import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
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
    public RiskAnalysis analyzeLatest() {
        String logs = gitHubActionsService.buildWorkflowSummary();

        MlRiskResponseDTO mlResult = mlRiskService.predictRisk(logs);

        return riskAnalysisService.saveMlAnalysis(logs, mlResult);
    }
}