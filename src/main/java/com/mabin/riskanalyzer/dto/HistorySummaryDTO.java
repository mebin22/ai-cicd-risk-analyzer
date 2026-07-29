package com.mabin.riskanalyzer.dto;

public class HistorySummaryDTO {

    private long successful;
    private long failed;
    private double averageConfidence;
    private double averageRiskScore;

    public HistorySummaryDTO() {
    }

    public HistorySummaryDTO(long successful,
                             long failed,
                             double averageConfidence,
                             double averageRiskScore) {
        this.successful = successful;
        this.failed = failed;
        this.averageConfidence = averageConfidence;
        this.averageRiskScore = averageRiskScore;
    }

    public long getSuccessful() {
        return successful;
    }

    public void setSuccessful(long successful) {
        this.successful = successful;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public double getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(double averageConfidence) {
        this.averageConfidence = averageConfidence;
    }

    public double getAverageRiskScore() {
        return averageRiskScore;
    }

    public void setAverageRiskScore(double averageRiskScore) {
        this.averageRiskScore = averageRiskScore;
    }
}