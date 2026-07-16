package com.mabin.riskanalyzer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubAnalysisResponseDTO {

    private Long id;
    private String source;
    private String riskLevel;
    private int riskScore;
    private Double confidence;
    private String deploymentDecision;
    private String recommendation;
}