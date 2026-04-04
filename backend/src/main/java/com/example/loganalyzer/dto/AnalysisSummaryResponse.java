package com.example.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSummaryResponse {
    private Long analysisId;
    private Long uploadedLogId;
    private String sourceType;
    private String fileName;
    private int totalLogs;
    private int totalInfo;
    private int totalWarn;
    private int totalError;
    private String primaryRootCause;
    private Double confidenceScore;
    private String summaryText;
    private List<TopCountDto> topRecurringErrors;
    private List<TopCountDto> topFailingServices;
    private List<String> recommendations;
}
