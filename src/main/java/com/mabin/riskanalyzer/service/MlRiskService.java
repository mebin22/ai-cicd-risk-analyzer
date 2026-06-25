package com.mabin.riskanalyzer.service;

import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class MlRiskService {

    private final RestClient restClient = RestClient.create();

    public MlRiskResponseDTO predictRisk(String logs) {
        Map<String, String> requestBody = Map.of("logs", logs);

        return restClient.post()
                .uri("http://localhost:5001/predict-risk")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(MlRiskResponseDTO.class);
    }
}