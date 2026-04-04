package com.example.loganalyzer.service;

import com.example.loganalyzer.analyzer.AnalysisComputation;
import com.example.loganalyzer.analyzer.LogAnalysisEngine;
import com.example.loganalyzer.model.entity.LogEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalysisEngineTest {

    private final LogAnalysisEngine engine = new LogAnalysisEngine();

    @Test
    void detectsTimeoutPatternAndCountsLevels() {
        List<LogEntry> entries = List.of(
                entry("ERROR", "PaymentService", "Timeout while calling provider", "timeout while calling provider"),
                entry("ERROR", "PaymentService", "Timeout while calling provider", "timeout while calling provider"),
                entry("WARN", "PaymentService", "Retry scheduled", "retry scheduled"),
                entry("INFO", "OrderService", "Order created", "order created")
        );

        AnalysisComputation result = engine.analyze(entries);

        assertEquals(4, result.getTotalLogs());
        assertEquals(2, result.getTotalError());
        assertEquals("Downstream dependency slowness or unavailability", result.getPrimaryCandidate().getIssueType());
        assertFalse(result.getRecommendations().isEmpty());
    }

    @Test
    void prioritizesProxyConnectivityFailureOverGeneric403AuthorizationMatch() {
        String proxyMessage = "mtalk.google.com:5228 error : could not connect through proxy proxy.cse.cuhk.edu.hk:8080 - proxy server cannot establish a connection with the target, status code 403";
        List<LogEntry> entries = List.of(
                entry("ERROR", "chrome.exe", proxyMessage, proxyMessage.toLowerCase()),
                entry("ERROR", "chrome.exe", proxyMessage, proxyMessage.toLowerCase()),
                entry("WARN", "chrome.exe", "Retrying proxy connection", "retrying proxy connection")
        );

        AnalysisComputation result = engine.analyze(entries);

        assertEquals("Proxy or network connectivity failure", result.getPrimaryCandidate().getIssueType());
        assertEquals("chrome.exe", result.getPrimaryCandidate().getServiceName());
        assertTrue(result.getSummaryText().contains("chrome.exe"));
        assertTrue(result.getSummaryText().contains("status code 403"));
    }

    private LogEntry entry(String level, String service, String message, String normalized) {
        return LogEntry.builder()
                .level(level)
                .serviceName(service)
                .message(message)
                .rawLine(message)
                .normalizedMessage(normalized)
                .build();
    }
}
