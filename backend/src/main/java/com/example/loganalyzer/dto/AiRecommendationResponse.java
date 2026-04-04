package com.example.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponse {
    private Long analysisId;
    private Long issueId;
    private String aiRecommendation;
}
