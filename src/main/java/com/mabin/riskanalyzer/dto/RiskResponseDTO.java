package com.mabin.riskanalyzer.dto;

public class RiskResponseDTO {

    private int riskScore;
    private String riskLevel;
    private String failureCause;
    private String recommendation;
    private String deploymentDecision;

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getFailureCause() {
        return failureCause;
    }

    public void setFailureCause(String failureCause) {
        this.failureCause = failureCause;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getDeploymentDecision() {
        return deploymentDecision;
    }

    public void setDeploymentDecision(String deploymentDecision) {
        this.deploymentDecision = deploymentDecision;
    }
}