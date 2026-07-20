package com.mabin.riskanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiCicdRiskAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCicdRiskAnalyzerApplication.class, args);
    }
}