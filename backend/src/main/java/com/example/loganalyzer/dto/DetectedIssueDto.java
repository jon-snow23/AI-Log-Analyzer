package com.example.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedIssueDto {
    private Long id;
    private String issueType;
    private String serviceName;
    private String severity;
    private Integer frequency;
    private String recommendation;
    private String aiRecommendation;
    private String evidence;
    private Double confidenceScore;
}
