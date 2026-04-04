package com.example.loganalyzer.analyzer;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class AnalysisComputation {
    int totalLogs;
    int totalInfo;
    int totalWarn;
    int totalError;
    List<Map.Entry<String, Long>> topErrors;
    List<Map.Entry<String, Long>> topServices;
    List<RootCauseCandidate> candidates;
    RootCauseCandidate primaryCandidate;
    String summaryText;
    List<String> recommendations;
}
