package com.example.loganalyzer.parser;

import java.util.List;

public interface LogParser {
    List<ParsedLogLine> parse(String rawLogs);
}
