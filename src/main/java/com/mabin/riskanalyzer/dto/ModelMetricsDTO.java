package com.mabin.riskanalyzer.dto;

public class ModelMetricsDTO {

    private Double accuracy;
    private Double precision;
    private Double recall;
    private Double f1Score;

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }
}