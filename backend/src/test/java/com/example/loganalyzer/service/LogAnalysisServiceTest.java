package com.example.loganalyzer.service;

import com.example.loganalyzer.analyzer.AnalysisComputation;
import com.example.loganalyzer.analyzer.LogAnalysisEngine;
import com.example.loganalyzer.analyzer.RootCauseCandidate;
import com.example.loganalyzer.dto.AnalysisSummaryResponse;
import com.example.loganalyzer.mapper.AnalysisMapper;
import com.example.loganalyzer.model.entity.AnalysisResult;
import com.example.loganalyzer.model.entity.LogEntry;
import com.example.loganalyzer.model.entity.UploadedLog;
import com.example.loganalyzer.model.enums.Severity;
import com.example.loganalyzer.parser.LogParser;
import com.example.loganalyzer.parser.ParsedLogLine;
import com.example.loganalyzer.repository.AnalysisResultRepository;
import com.example.loganalyzer.repository.DetectedIssueRepository;
import com.example.loganalyzer.repository.LogEntryRepository;
import com.example.loganalyzer.repository.UploadedLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAnalysisServiceTest {

    @Mock
    private UploadedLogRepository uploadedLogRepository;
    @Mock
    private LogEntryRepository logEntryRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private DetectedIssueRepository detectedIssueRepository;
    @Mock
    private LogParser logParser;
    @Mock
    private LogAnalysisEngine analysisEngine;
    @Mock
    private AnalysisMapper analysisMapper;
    @Mock
    private AiRecommendationService aiRecommendationService;

    @InjectMocks
    private LogAnalysisService logAnalysisService;

    @Test
    void analyzeTextPersistsAndReturnsSummary() {
        UploadedLog upload = UploadedLog.builder().id(1L).fileName("pasted-logs.txt").uploadTime(OffsetDateTime.now()).build();
        AnalysisResult result = AnalysisResult.builder().id(2L).uploadedLog(upload).totalLogs(1).totalInfo(0).totalWarn(0).totalError(1).build();
        ParsedLogLine parsed = ParsedLogLine.builder()
                .level("ERROR")
                .serviceName("PaymentService")
                .message("Timeout while connecting")
                .rawLine("2026-04-04 10:01:15 ERROR PaymentService - Timeout while connecting")
                .normalizedMessage("timeout while connecting")
                .build();
        AnalysisComputation computation = AnalysisComputation.builder()
                .totalLogs(1)
                .totalInfo(0)
                .totalWarn(0)
                .totalError(1)
                .topErrors(List.of())
                .topServices(List.of())
                .candidates(List.of(RootCauseCandidate.builder()
                        .issueType("Downstream dependency slowness or unavailability")
                        .confidenceScore(0.82)
                        .evidence("Timeout while connecting")
                        .recommendation("Inspect dependency health.")
                        .severity(Severity.HIGH)
                        .serviceName("PaymentService")
                        .frequency(1)
                        .build()))
                .primaryCandidate(RootCauseCandidate.builder()
                        .issueType("Downstream dependency slowness or unavailability")
                        .confidenceScore(0.82)
                        .evidence("Timeout while connecting")
                        .recommendation("Inspect dependency health.")
                        .severity(Severity.HIGH)
                        .serviceName("PaymentService")
                        .frequency(1)
                        .build())
                .summaryText("summary")
                .recommendations(List.of("Inspect dependency health."))
                .build();
        AnalysisSummaryResponse mapped = AnalysisSummaryResponse.builder().analysisId(2L).totalLogs(1).primaryRootCause("Downstream dependency slowness or unavailability").build();

        when(uploadedLogRepository.save(any(UploadedLog.class))).thenReturn(upload);
        when(logParser.parse(any(String.class))).thenReturn(List.of(parsed));
        when(logEntryRepository.saveAll(ArgumentMatchers.<LogEntry>anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(analysisEngine.analyze(ArgumentMatchers.<LogEntry>anyList())).thenReturn(computation);
        when(analysisResultRepository.save(any(AnalysisResult.class))).thenReturn(result);
        when(detectedIssueRepository.saveAll(ArgumentMatchers.anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(analysisMapper.toSummaryResponse(any(AnalysisResult.class), any(UploadedLog.class), ArgumentMatchers.anyList(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList()))
                .thenReturn(mapped);

        AnalysisSummaryResponse response = logAnalysisService.analyzeText("2026-04-04 10:01:15 ERROR PaymentService - Timeout while connecting");

        assertEquals(2L, response.getAnalysisId());
        assertEquals(1, response.getTotalLogs());
        assertEquals("Downstream dependency slowness or unavailability", response.getPrimaryRootCause());
    }
}
