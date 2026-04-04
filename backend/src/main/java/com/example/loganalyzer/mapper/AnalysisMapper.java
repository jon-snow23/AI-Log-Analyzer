package com.example.loganalyzer.mapper;

import com.example.loganalyzer.dto.*;
import com.example.loganalyzer.model.entity.AnalysisResult;
import com.example.loganalyzer.model.entity.DetectedIssue;
import com.example.loganalyzer.model.entity.LogEntry;
import com.example.loganalyzer.model.entity.UploadedLog;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AnalysisMapper {

    public AnalysisSummaryResponse toSummaryResponse(
            AnalysisResult result,
            UploadedLog uploadedLog,
            List<Map.Entry<String, Long>> topErrors,
            List<Map.Entry<String, Long>> topServices,
            List<String> recommendations
    ) {
        return AnalysisSummaryResponse.builder()
                .analysisId(result.getId())
                .uploadedLogId(uploadedLog.getId())
                .sourceType(uploadedLog.getSourceType().name())
                .fileName(uploadedLog.getFileName())
                .totalLogs(result.getTotalLogs())
                .totalInfo(result.getTotalInfo())
                .totalWarn(result.getTotalWarn())
                .totalError(result.getTotalError())
                .primaryRootCause(result.getPrimaryRootCause())
                .confidenceScore(result.getConfidenceScore())
                .summaryText(result.getSummaryText())
                .topRecurringErrors(toTopCountDtos(topErrors))
                .topFailingServices(toTopCountDtos(topServices))
                .recommendations(recommendations)
                .build();
    }

    public List<TopCountDto> toTopCountDtos(List<Map.Entry<String, Long>> counts) {
        return counts.stream()
                .map(entry -> TopCountDto.builder().name(entry.getKey()).count(entry.getValue()).build())
                .toList();
    }

    public DetectedIssueDto toIssueDto(DetectedIssue issue) {
        return DetectedIssueDto.builder()
                .id(issue.getId())
                .issueType(issue.getIssueType())
                .serviceName(issue.getServiceName())
                .severity(issue.getSeverity().name())
                .frequency(issue.getFrequency())
                .recommendation(issue.getRecommendation())
                .evidence(issue.getEvidenceText())
                .confidenceScore(issue.getConfidenceScore())
                .build();
    }

    public DetectedIssueDto withAiRecommendation(DetectedIssueDto issue, String aiRecommendation) {
        issue.setAiRecommendation(aiRecommendation);
        return issue;
    }

    public PagedResponse<LogEntryDto> toPagedLogResponse(Page<LogEntry> page) {
        return PagedResponse.<LogEntryDto>builder()
                .content(page.getContent().stream().map(this::toEntryDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public LogEntryDto toEntryDto(LogEntry entry) {
        return LogEntryDto.builder()
                .id(entry.getId())
                .timestamp(entry.getTimestamp() == null ? null : entry.getTimestamp().toString())
                .level(entry.getLevel())
                .serviceName(entry.getServiceName())
                .message(entry.getMessage())
                .exceptionType(entry.getExceptionType())
                .rawLine(entry.getRawLine())
                .build();
    }
}
