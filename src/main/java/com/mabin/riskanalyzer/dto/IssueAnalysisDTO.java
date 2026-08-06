package com.mabin.riskanalyzer.dto;

public class IssueAnalysisDTO {

    private String failureCause;
    private String recommendation;

    public IssueAnalysisDTO() {
    }

    public IssueAnalysisDTO(
            String failureCause,
            String recommendation
    ) {
        this.failureCause = failureCause;
        this.recommendation = recommendation;
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
}