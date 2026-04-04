package com.example.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDto {
    private Long id;
    private String timestamp;
    private String level;
    private String serviceName;
    private String message;
    private String exceptionType;
    private String rawLine;
}
