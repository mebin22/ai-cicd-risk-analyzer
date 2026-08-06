package com.mabin.riskanalyzer.controller;

import com.mabin.riskanalyzer.dto.GitHubAnalysisResponseDTO;
import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
import com.mabin.riskanalyzer.dto.ModelMetricsDTO;
import com.mabin.riskanalyzer.model.RiskAnalysis;
import com.mabin.riskanalyzer.service.GitHubActionsService;
import com.mabin.riskanalyzer.service.MlRiskService;
import com.mabin.riskanalyzer.service.OllamaIssueAnalysisService;
import com.mabin.riskanalyzer.service.RiskAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mabin.riskanalyzer.service.IssueDetectionService;

@RestController
public class GitHubActionsController {

    private final GitHubActionsService gitHubActionsService;
    private final MlRiskService mlRiskService;
    private final RiskAnalysisService riskAnalysisService;
    private final OllamaIssueAnalysisService ollamaIssueAnalysisService;
    private final IssueDetectionService issueDetectionService;


    public GitHubActionsController(
            GitHubActionsService gitHubActionsService,
            MlRiskService mlRiskService,
            RiskAnalysisService riskAnalysisService,
            OllamaIssueAnalysisService ollamaIssueAnalysisService,
            IssueDetectionService issueDetectionService
    ) {
        this.gitHubActionsService = gitHubActionsService;
        this.mlRiskService = mlRiskService;
        this.riskAnalysisService = riskAnalysisService;
        this.ollamaIssueAnalysisService =
                ollamaIssueAnalysisService;
        this.issueDetectionService = issueDetectionService;
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

        /*
         * Get the latest workflow run ID once.
         *
         * Reusing the same ID helps prevent the application
         * from analysing one workflow and saving another
         * workflow's run ID.
         */
        Long runId =
                gitHubActionsService.getLatestRunId();


        /*
         * The workflow summary contains:
         *
         * - Workflow status
         * - Workflow conclusion
         * - Job status
         * - Job conclusion
         * - Step names and results
         *
         * This text is sent to the Logistic Regression model.
         */
        String workflowSummary =
                gitHubActionsService.buildWorkflowSummary(
                        runId
                );

        System.out.println(
                "===== WORKFLOW SUMMARY FOR ML ====="
        );
        System.out.println(workflowSummary);
        System.out.println(
                "==================================="
        );


        /*
         * Download the actual GitHub Actions console log.
         *
         * Ollama uses this detailed log to detect the real
         * problem and generate a specific recommendation.
         */
        String jobLog =
                gitHubActionsService.downloadLatestJobLog();
        System.out.println("===== ACTUAL GITHUB JOB LOG =====");
        System.out.println(jobLog);
        System.out.println("=================================");

        System.out.println(
                "Downloaded GitHub job-log characters: "
                        + jobLog.length()
        );


        /*
         * Logistic Regression predicts:
         *
         * - LOW, MEDIUM or HIGH
         * - Risk score
         * - Confidence
         * - Deployment decision
         */
        MlRiskResponseDTO mlResult =
                mlRiskService.predictRisk(
                        workflowSummary
                );


        /*
         * Ollama analyses the actual console log and returns:
         *
         * - The detected failure cause
         * - An issue-specific recommendation
         */
        String failureCause =
                issueDetectionService.detect(jobLog);

        String recommendation =
                ollamaIssueAnalysisService.generateRecommendation(
                        failureCause,
                        mlResult.getRiskLevel()
                );

        mlResult.setFailureCause(failureCause);
        mlResult.setRecommendation(recommendation);


        /*
         * Save the complete analysis in PostgreSQL.
         */
        RiskAnalysis saved =
                riskAnalysisService.saveMlAnalysis(
                        jobLog,
                        mlResult,
                        runId
                );


        /*
         * Build the response displayed in Postman
         * and on the dashboard.
         */
        GitHubAnalysisResponseDTO response =
                new GitHubAnalysisResponseDTO();

        response.setId(
                saved.getId()
        );

        response.setSource(
                "GitHub Actions"
        );

        response.setRiskLevel(
                saved.getRiskLevel()
        );

        response.setRiskScore(
                saved.getRiskScore()
        );

        response.setConfidence(
                saved.getConfidence()
        );

        response.setFailureCause(
                saved.getFailureCause()
        );

        response.setDeploymentDecision(
                saved.getDeploymentDecision()
        );

        response.setRecommendation(
                saved.getRecommendation()
        );

        return response;
    }


    @GetMapping("/api/ml/metrics")
    public ModelMetricsDTO getMetrics() {
        return mlRiskService.getMetrics();
    }
}