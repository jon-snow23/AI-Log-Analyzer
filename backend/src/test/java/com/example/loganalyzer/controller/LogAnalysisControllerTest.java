package com.example.loganalyzer.controller;

import com.example.loganalyzer.dto.AnalysisSummaryResponse;
import com.example.loganalyzer.dto.AiRecommendationResponse;
import com.example.loganalyzer.dto.AiRecommendationsResponse;
import com.example.loganalyzer.service.LogAnalysisService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogAnalysisController.class)
class LogAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogAnalysisService logAnalysisService;

    @Test
    void analyzeTextReturnsSummary() throws Exception {
        Mockito.when(logAnalysisService.analyzeText(Mockito.anyString()))
                .thenReturn(AnalysisSummaryResponse.builder().analysisId(99L).totalLogs(10).primaryRootCause("Timeout").build());

        mockMvc.perform(post("/api/logs/analyze-text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawLogs\":\"2026-04-04 10:01:15 ERROR PaymentService - Timeout\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(99L))
                .andExpect(jsonPath("$.totalLogs").value(10));
    }

    @Test
    void uploadReturnsSummary() throws Exception {
        Mockito.when(logAnalysisService.analyzeUpload(Mockito.any()))
                .thenReturn(AnalysisSummaryResponse.builder().analysisId(12L).totalLogs(8).build());
        MockMultipartFile file = new MockMultipartFile("file", "sample.log", "text/plain", "line".getBytes());

        mockMvc.perform(multipart("/api/logs/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(12L));
    }

    @Test
    void generateIssueRecommendationReturnsPayload() throws Exception {
        Mockito.when(logAnalysisService.generateIssueRecommendation(99L, 7L))
                .thenReturn(AiRecommendationResponse.builder()
                        .analysisId(99L)
                        .issueId(7L)
                        .aiRecommendation("Inspect recent RBAC changes.")
                        .build());

        mockMvc.perform(post("/api/logs/99/issues/7/ai-recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(99L))
                .andExpect(jsonPath("$.issueId").value(7L))
                .andExpect(jsonPath("$.aiRecommendation").value("Inspect recent RBAC changes."));
    }

    @Test
    void generateOverallRecommendationsReturnsPayload() throws Exception {
        Mockito.when(logAnalysisService.generateOverallRecommendations(99L))
                .thenReturn(AiRecommendationsResponse.builder()
                        .analysisId(99L)
                        .recommendations(java.util.List.of("Prioritize proxy validation.", "Review RBAC changes."))
                        .build());

        mockMvc.perform(post("/api/logs/99/ai-recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(99L))
                .andExpect(jsonPath("$.recommendations[0]").value("Prioritize proxy validation."));
    }
}
