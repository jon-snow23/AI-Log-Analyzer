package com.example.loganalyzer.parser;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class ParsedLogLine {
    OffsetDateTime timestamp;
    String level;
    String serviceName;
    String message;
    String exceptionType;
    String rawLine;
    String normalizedMessage;
}
