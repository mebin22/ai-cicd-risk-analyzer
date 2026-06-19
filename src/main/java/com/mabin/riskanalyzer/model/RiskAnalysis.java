package com.mabin.riskanalyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RiskAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String logs;

    private int riskScore;
    private String riskLevel;
    private String failureCause;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    private String deploymentDecision;
    private LocalDateTime timestamp;
}