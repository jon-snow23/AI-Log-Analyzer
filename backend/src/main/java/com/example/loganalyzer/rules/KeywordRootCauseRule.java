package com.example.loganalyzer.rules;

import com.example.loganalyzer.analyzer.RootCauseCandidate;
import com.example.loganalyzer.model.entity.LogEntry;
import com.example.loganalyzer.model.enums.Severity;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeywordRootCauseRule implements RootCauseRule {

    private final String keyword;
    private final String issueType;
    private final String recommendation;
    private final Severity severity;
    private final double baseConfidence;

    public KeywordRootCauseRule(String keyword, String issueType, String recommendation, Severity severity, double baseConfidence) {
        this.keyword = keyword.toLowerCase(Locale.ROOT);
        this.issueType = issueType;
        this.recommendation = recommendation;
        this.severity = severity;
        this.baseConfidence = baseConfidence;
    }

    @Override
    public Optional<RootCauseCandidate> evaluate(List<LogEntry> entries) {
        List<LogEntry> matched = entries.stream()
                .filter(entry -> contains(entry.getMessage(), keyword) || contains(entry.getRawLine(), keyword))
                .toList();
        if (matched.isEmpty()) {
            return Optional.empty();
        }
        String service = matched.stream()
                .collect(Collectors.groupingBy(entry -> entry.getServiceName() == null ? "Unknown" : entry.getServiceName(), Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("Unknown");
        int frequency = matched.size();
        double confidence = Math.min(0.99, baseConfidence + (Math.min(frequency, 10) * 0.02));
        String evidence = matched.stream()
                .limit(3)
                .map(LogEntry::getRawLine)
                .collect(Collectors.joining(" | "));
        return Optional.of(RootCauseCandidate.builder()
                .issueType(issueType)
                .confidenceScore(confidence)
                .recommendation(recommendation)
                .severity(severity)
                .serviceName(service)
                .frequency(frequency)
                .evidence(evidence)
                .build());
    }

    private boolean contains(String value, String token) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(token);
    }
}
