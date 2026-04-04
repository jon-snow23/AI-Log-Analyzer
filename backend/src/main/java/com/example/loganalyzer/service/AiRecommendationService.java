package com.example.loganalyzer.service;

import com.example.loganalyzer.model.entity.AnalysisResult;
import com.example.loganalyzer.model.entity.DetectedIssue;
import com.example.loganalyzer.dto.AnalysisSummaryResponse;

import java.util.List;

public interface AiRecommendationService {
    String generateIssueRecommendation(AnalysisResult analysisResult, DetectedIssue issue);
    List<String> generateOverallRecommendations(AnalysisResult analysisResult, AnalysisSummaryResponse summary, List<DetectedIssue> issues);
}
