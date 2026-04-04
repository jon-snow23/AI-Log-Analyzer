package com.example.loganalyzer.analyzer;

import com.example.loganalyzer.model.enums.Severity;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RootCauseCandidate {
    String issueType;
    double confidenceScore;
    String evidence;
    String recommendation;
    Severity severity;
    String serviceName;
    int frequency;
}
