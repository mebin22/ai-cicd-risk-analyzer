package com.mabin.riskanalyzer.dto;

public class MlRiskResponseDTO {

    private int riskScore;
    private String riskLevel;
    private double confidence;
    private String deploymentDecision;
    private String recommendation;

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

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getDeploymentDecision() {
        return deploymentDecision;
    }

    public void setDeploymentDecision(String deploymentDecision) {
        this.deploymentDecision = deploymentDecision;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}