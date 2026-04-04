package com.example.loganalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalyzeTextRequest {
    @NotBlank
    private String rawLogs;
}
