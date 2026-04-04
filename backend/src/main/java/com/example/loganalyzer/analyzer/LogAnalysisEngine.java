package com.example.loganalyzer.analyzer;

import com.example.loganalyzer.model.entity.LogEntry;
import com.example.loganalyzer.model.enums.Severity;
import com.example.loganalyzer.rules.KeywordRootCauseRule;
import com.example.loganalyzer.rules.RootCauseRule;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LogAnalysisEngine {

    private final List<RootCauseRule> rootCauseRules;

    public LogAnalysisEngine() {
        this.rootCauseRules = List.of(
                new KeywordRootCauseRule("could not connect through proxy", "Proxy or network connectivity failure", "Verify proxy settings, DNS, egress rules, and target reachability.", Severity.HIGH, 0.93),
                new KeywordRootCauseRule("proxy server cannot establish a connection", "Proxy or network connectivity failure", "Verify proxy settings, DNS, egress rules, and target reachability.", Severity.HIGH, 0.94),
                new KeywordRootCauseRule("network unreachable", "Proxy or network connectivity failure", "Verify proxy settings, DNS, egress rules, and target reachability.", Severity.HIGH, 0.88),
                new KeywordRootCauseRule("timeout", "Downstream dependency slowness or unavailability", "Inspect upstream latency, retry policy, and dependency health.", Severity.HIGH, 0.72),
                new KeywordRootCauseRule("connection refused", "Target service or database unavailable", "Validate that the destination host, port, and service instance are accepting connections.", Severity.HIGH, 0.84),
                new KeywordRootCauseRule("nullpointerexception", "Application logic bug or missing null handling", "Inspect the offending code path and add null guards or contract validation.", Severity.HIGH, 0.8),
                new KeywordRootCauseRule("outofmemoryerror", "Memory pressure or leak", "Check heap sizing, memory spikes, and long-lived object retention.", Severity.HIGH, 0.9),
                new KeywordRootCauseRule("too many connections", "Database pool exhaustion", "Increase pool efficiency, review connection leaks, and tune max pool size.", Severity.HIGH, 0.86),
                new KeywordRootCauseRule("401", "Authentication failure", "Verify credentials, token freshness, and auth middleware configuration.", Severity.MEDIUM, 0.71),
                new KeywordRootCauseRule("403", "Permission or authorization failure", "Review RBAC, policy changes, and service account permissions.", Severity.MEDIUM, 0.73),
                new KeywordRootCauseRule("access denied", "Permission or authorization failure", "Review RBAC, policy changes, and service account permissions.", Severity.MEDIUM, 0.76),
                new KeywordRootCauseRule("broken pipe", "Client disconnect or network interruption", "Inspect load balancer resets, client timeouts, and network reliability.", Severity.MEDIUM, 0.69)
        );
    }

    public AnalysisComputation analyze(List<LogEntry> entries) {
        int totalLogs = entries.size();
        int totalInfo = countByLevel(entries, "INFO");
        int totalWarn = countByLevel(entries, "WARN");
        int totalError = countByLevel(entries, "ERROR");

        List<Map.Entry<String, Long>> topErrors = entries.stream()
                .filter(entry -> "ERROR".equalsIgnoreCase(entry.getLevel()))
                .collect(Collectors.groupingBy(entry -> valueOrFallback(entry.getNormalizedMessage(), entry.getMessage()), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();

        List<Map.Entry<String, Long>> topServices = entries.stream()
                .filter(entry -> "ERROR".equalsIgnoreCase(entry.getLevel()) || "WARN".equalsIgnoreCase(entry.getLevel()))
                .collect(Collectors.groupingBy(entry -> valueOrFallback(entry.getServiceName(), "Unknown"), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();

        List<RootCauseCandidate> candidates = new ArrayList<>();
        rootCauseRules.stream()
                .map(rule -> rule.evaluate(entries))
                .flatMap(Optional::stream)
                .forEach(candidates::add);

        addAnomalyCandidates(entries, totalLogs, totalError, candidates);
        candidates.sort(Comparator.comparing(RootCauseCandidate::getConfidenceScore).reversed()
                .thenComparing(RootCauseCandidate::getFrequency, Comparator.reverseOrder()));
        RootCauseCandidate primary = candidates.isEmpty() ? defaultCandidate(totalError, totalWarn) : candidates.get(0);

        List<String> recommendations = candidates.stream()
                .map(RootCauseCandidate::getRecommendation)
                .distinct()
                .limit(4)
                .toList();
        if (recommendations.isEmpty()) {
            recommendations = List.of("Review correlated services around the peak error window and compare recent deployments.");
        }

        String summary = buildSummary(totalLogs, totalWarn, totalError, primary, topServices, topErrors);
        return AnalysisComputation.builder()
                .totalLogs(totalLogs)
                .totalInfo(totalInfo)
                .totalWarn(totalWarn)
                .totalError(totalError)
                .topErrors(topErrors)
                .topServices(topServices)
                .candidates(candidates)
                .primaryCandidate(primary)
                .summaryText(summary)
                .recommendations(recommendations)
                .build();
    }

    private void addAnomalyCandidates(List<LogEntry> entries, int totalLogs, int totalError, List<RootCauseCandidate> candidates) {
        if (totalLogs > 0 && ((double) totalError / totalLogs) >= 0.35) {
            candidates.add(RootCauseCandidate.builder()
                    .issueType("Unusually high error ratio")
                    .confidenceScore(0.68)
                    .evidence(totalError + " of " + totalLogs + " entries are errors.")
                    .recommendation("Inspect the dominant failing service and compare with baseline error rates.")
                    .severity(Severity.HIGH)
                    .serviceName("Multiple")
                    .frequency(totalError)
                    .build());
        }

        Map<String, Long> repeatedErrors = entries.stream()
                .filter(entry -> "ERROR".equalsIgnoreCase(entry.getLevel()))
                .collect(Collectors.groupingBy(entry -> valueOrFallback(entry.getNormalizedMessage(), entry.getMessage()), Collectors.counting()));
        repeatedErrors.entrySet().stream()
                .filter(entry -> entry.getValue() >= 5)
                .findFirst()
                .ifPresent(entry -> candidates.add(RootCauseCandidate.builder()
                        .issueType("Repeated recurring error pattern")
                        .confidenceScore(0.76)
                        .evidence(entry.getKey() + " repeated " + entry.getValue() + " times.")
                        .recommendation("Trace this message across recent deploys, downstream dependencies, and retry loops.")
                        .severity(Severity.HIGH)
                        .serviceName("Multiple")
                        .frequency(entry.getValue().intValue())
                        .build()));
    }

    private RootCauseCandidate defaultCandidate(int totalError, int totalWarn) {
        if (totalError > 0) {
            return RootCauseCandidate.builder()
                    .issueType("General application instability")
                    .confidenceScore(0.56)
                    .evidence("Errors were detected but no high-confidence keyword rule matched.")
                    .recommendation("Review the top recurring errors and correlate them with deployment or infrastructure events.")
                    .severity(Severity.MEDIUM)
                    .serviceName("Unknown")
                    .frequency(totalError)
                    .build();
        }
        return RootCauseCandidate.builder()
                .issueType("No major incident pattern detected")
                .confidenceScore(0.42)
                .evidence("Log set contains mostly informational or warning entries.")
                .recommendation("Monitor warning growth and inspect noisy services if the pattern persists.")
                .severity(totalWarn > 0 ? Severity.LOW : Severity.LOW)
                .serviceName("Unknown")
                .frequency(totalWarn)
                .build();
    }

    private String buildSummary(int totalLogs, int totalWarn, int totalError, RootCauseCandidate primary, List<Map.Entry<String, Long>> topServices, List<Map.Entry<String, Long>> topErrors) {
        String topService = topServices.isEmpty() ? "Unknown" : topServices.get(0).getKey();
        String topError = topErrors.isEmpty() ? "n/a" : topErrors.get(0).getKey();
        return "Processed " + totalLogs + " log entries with " + totalError + " errors and " + totalWarn + " warnings. "
                + "Primary root-cause candidate: " + primary.getIssueType() + " (" + Math.round(primary.getConfidenceScore() * 100) + "% confidence). "
                + "Most impacted service: " + topService + ". Dominant recurring error: " + topError + ".";
    }

    private int countByLevel(List<LogEntry> entries, String level) {
        return (int) entries.stream().filter(entry -> level.equalsIgnoreCase(entry.getLevel())).count();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
