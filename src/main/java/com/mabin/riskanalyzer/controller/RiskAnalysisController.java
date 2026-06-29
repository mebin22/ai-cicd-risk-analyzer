package com.mabin.riskanalyzer.controller;

import com.mabin.riskanalyzer.dto.*;
import com.mabin.riskanalyzer.model.RiskAnalysis;
import com.mabin.riskanalyzer.repository.RiskAnalysisRepository;
import com.mabin.riskanalyzer.service.GeminiService;
import com.mabin.riskanalyzer.service.MlRiskService;
import com.mabin.riskanalyzer.service.RiskAnalysisService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;
    private final GeminiService geminiService;
    private final MlRiskService mlRiskService;
    private final RiskAnalysisRepository riskAnalysisRepository;

    public RiskAnalysisController(RiskAnalysisService riskAnalysisService,
                                  GeminiService geminiService,
                                  MlRiskService mlRiskService,
                                  RiskAnalysisRepository riskAnalysisRepository) {
        this.riskAnalysisService = riskAnalysisService;
        this.geminiService = geminiService;
        this.mlRiskService = mlRiskService;
        this.riskAnalysisRepository = riskAnalysisRepository;
    }

    @PostMapping("/analyze")
    public RiskResponseDTO analyzeRisk(@RequestBody RiskRequestDTO request) {
        return riskAnalysisService.analyzeRisk(request);
    }

    @PostMapping("/analyze-ml")
    public MlRiskResponseDTO analyzeWithML(@RequestBody RiskRequestDTO request) {

        MlRiskResponseDTO response =
                mlRiskService.predictRisk(request.getLogs());

        riskAnalysisService.saveMlAnalysis(
                request.getLogs(),
                response
        );

        return response;
    }

    @GetMapping("/scenarios")
    public String getScenarios() {
        return """
            Supported test scenarios:
            1. Unit test failure
            2. Docker build error
            3. Dependency issue
            4. Deployment failure
            5. Successful build
            """;
    }

    @PostMapping("/analyze-ai")
    public RiskResponseDTO analyzeWithAI(@RequestBody RiskRequestDTO request) {
        return geminiService.analyzeLogsWithAI(request.getLogs());
    }

    @GetMapping("/history")
    public List<RiskAnalysis> getHistory() {
        return riskAnalysisRepository.findAll();
    }

    @GetMapping("/stats")
    public DashboardStatsDTO getStats() {

        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setTotalAnalyses(riskAnalysisRepository.count());
        stats.setHighRiskCount(riskAnalysisRepository.countByRiskLevel("HIGH"));
        stats.setMediumRiskCount(riskAnalysisRepository.countByRiskLevel("MEDIUM"));
        stats.setLowRiskCount(riskAnalysisRepository.countByRiskLevel("LOW"));

        Double avg = riskAnalysisRepository.getAverageRiskScore();
        stats.setAverageRiskScore(avg != null ? avg : 0);

        return stats;
    }

    @GetMapping("/recent")
    public List<RiskAnalysis> getRecentAnalyses() {
        return riskAnalysisRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(0, 10));
    }

    @GetMapping("/trend")
    public RiskTrendDTO getRiskTrend() {

        RiskTrendDTO trend = new RiskTrendDTO();

        trend.setHighRisk(riskAnalysisRepository.countByRiskLevel("HIGH"));
        trend.setMediumRisk(riskAnalysisRepository.countByRiskLevel("MEDIUM"));
        trend.setLowRisk(riskAnalysisRepository.countByRiskLevel("LOW"));

        return trend;
    }
}