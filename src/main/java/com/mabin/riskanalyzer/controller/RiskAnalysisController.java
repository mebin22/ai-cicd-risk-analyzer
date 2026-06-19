package com.mabin.riskanalyzer.controller;

import com.mabin.riskanalyzer.dto.RiskRequestDTO;
import com.mabin.riskanalyzer.dto.RiskResponseDTO;
import com.mabin.riskanalyzer.model.RiskAnalysis;
import com.mabin.riskanalyzer.repository.RiskAnalysisRepository;
import com.mabin.riskanalyzer.service.GeminiService;
import com.mabin.riskanalyzer.service.RiskAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;
    private final GeminiService geminiService;
    private final RiskAnalysisRepository riskAnalysisRepository;

    public RiskAnalysisController(RiskAnalysisService riskAnalysisService,
                                  GeminiService geminiService, RiskAnalysisRepository riskAnalysisRepository) {
        this.riskAnalysisService = riskAnalysisService;
        this.geminiService = geminiService;
        this.riskAnalysisRepository = riskAnalysisRepository;
    }

    @PostMapping("/analyze")
    public RiskResponseDTO analyzeRisk(@RequestBody RiskRequestDTO request) {
        return riskAnalysisService.analyzeRisk(request);
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
}