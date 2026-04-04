package com.example.loganalyzer.rules;

import com.example.loganalyzer.analyzer.RootCauseCandidate;
import com.example.loganalyzer.model.entity.LogEntry;

import java.util.List;
import java.util.Optional;

public interface RootCauseRule {
    Optional<RootCauseCandidate> evaluate(List<LogEntry> entries);
}
