package com.example.loganalyzer.controller;

import com.example.loganalyzer.dto.*;
import com.example.loganalyzer.service.LogAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LogAnalysisController {

    private final LogAnalysisService logAnalysisService;

    public LogAnalysisController(LogAnalysisService logAnalysisService) {
        this.logAnalysisService = logAnalysisService;
    }

    @PostMapping(value = "/logs/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisSummaryResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(logAnalysisService.analyzeUpload(file));
    }

    @PostMapping("/logs/analyze-text")
    public ResponseEntity<AnalysisSummaryResponse> analyzeText(@Valid @RequestBody AnalyzeTextRequest request) {
        return ResponseEntity.ok(logAnalysisService.analyzeText(request.getRawLogs()));
    }

    @GetMapping("/logs/{analysisId}/summary")
    public ResponseEntity<AnalysisSummaryResponse> getSummary(@PathVariable Long analysisId) {
        return ResponseEntity.ok(logAnalysisService.getSummary(analysisId));
    }

    @GetMapping("/logs/{analysisId}/issues")
    public ResponseEntity<List<DetectedIssueDto>> getIssues(@PathVariable Long analysisId) {
        return ResponseEntity.ok(logAnalysisService.getIssues(analysisId));
    }

    @PostMapping("/logs/{analysisId}/issues/{issueId}/ai-recommendation")
    public ResponseEntity<AiRecommendationResponse> generateIssueRecommendation(@PathVariable Long analysisId,
                                                                                @PathVariable Long issueId) {
        return ResponseEntity.ok(logAnalysisService.generateIssueRecommendation(analysisId, issueId));
    }

    @PostMapping("/logs/{analysisId}/ai-recommendations")
    public ResponseEntity<AiRecommendationsResponse> generateOverallRecommendations(@PathVariable Long analysisId) {
        return ResponseEntity.ok(logAnalysisService.generateOverallRecommendations(analysisId));
    }

    @GetMapping("/logs/{analysisId}/entries")
    public ResponseEntity<PagedResponse<LogEntryDto>> getEntries(
            @PathVariable Long analysisId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(logAnalysisService.getEntries(analysisId, level, service, search, page, size));
    }

    @GetMapping("/logs/{analysisId}/export")
    public ResponseEntity<ExportResponse> export(@PathVariable Long analysisId) {
        return ResponseEntity.ok(logAnalysisService.export(analysisId));
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }
}
