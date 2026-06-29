package com.mabin.riskanalyzer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskTrendDTO {

    private long highRisk;
    private long mediumRisk;
    private long lowRisk;
}