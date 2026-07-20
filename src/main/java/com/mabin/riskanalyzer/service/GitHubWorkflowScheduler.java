package com.mabin.riskanalyzer.service;

import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GitHubWorkflowScheduler {

    private final GitHubActionsService gitHubActionsService;
    private final MlRiskService mlRiskService;
    private final RiskAnalysisService riskAnalysisService;

    public GitHubWorkflowScheduler(
            GitHubActionsService gitHubActionsService,
            MlRiskService mlRiskService,
            RiskAnalysisService riskAnalysisService) {

        this.gitHubActionsService = gitHubActionsService;
        this.mlRiskService = mlRiskService;
        this.riskAnalysisService = riskAnalysisService;
    }

    @Scheduled(
            initialDelayString = "${github.analysis.initial-delay-ms:30000}",
            fixedDelayString = "${github.analysis.interval-ms:300000}"
    )
    public void analyzeLatestWorkflowAutomatically() {

        try {
            Long runId = gitHubActionsService.getLatestRunId();

            if (riskAnalysisService.isGithubRunAlreadyAnalyzed(runId)) {
                System.out.println(
                        "GitHub workflow run " + runId
                                + " has already been analyzed."
                );
                return;
            }

            String logs =
                    gitHubActionsService.buildWorkflowSummary(runId);

            MlRiskResponseDTO mlResult =
                    mlRiskService.predictRisk(logs);

            riskAnalysisService.saveMlAnalysis(
                    logs,
                    mlResult,
                    runId
            );

            System.out.println(
                    "Automatically analyzed GitHub workflow run: "
                            + runId
            );

        } catch (Exception exception) {
            System.err.println(
                    "Automatic GitHub workflow analysis failed: "
                            + exception.getMessage()
            );
        }
    }
}