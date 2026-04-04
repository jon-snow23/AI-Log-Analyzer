package com.example.loganalyzer.service;

import com.example.loganalyzer.config.AiProperties;
import com.example.loganalyzer.dto.AnalysisSummaryResponse;
import com.example.loganalyzer.dto.TopCountDto;
import com.example.loganalyzer.model.entity.AnalysisResult;
import com.example.loganalyzer.model.entity.DetectedIssue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiAiRecommendationService implements AiRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiRecommendationService.class);

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAiRecommendationService(AiProperties properties,
                                         RestTemplateBuilder restTemplateBuilder,
                                         ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        int timeoutSeconds = resolveTimeoutSeconds(properties);
        Duration timeout = Duration.ofSeconds(Math.max(timeoutSeconds, 1));
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }

    @Override
    public String generateIssueRecommendation(AnalysisResult analysisResult, DetectedIssue issue) {
        String provider = validateConfiguration();

        try {
            String prompt = buildPrompt(analysisResult, issue);
            if ("gemini".equalsIgnoreCase(provider)) {
                return generateWithGemini(prompt);
            }
            return generateWithOpenRouter(prompt);
        } catch (HttpClientErrorException exception) {
            log.warn("{} client error for issue {}", providerLabel(provider), issue.getId(), exception);
            throw new IllegalStateException(providerLabel(provider) + " rejected the request: HTTP " + exception.getStatusCode().value() + ".");
        } catch (HttpServerErrorException exception) {
            log.warn("{} server error for issue {}", providerLabel(provider), issue.getId(), exception);
            throw new IllegalStateException(providerLabel(provider) + " service is temporarily unavailable: HTTP " + exception.getStatusCode().value() + ".");
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("{} AI recommendation generation failed for issue {}", providerLabel(provider), issue.getId(), exception);
            throw new IllegalStateException(providerLabel(provider) + " request failed before a recommendation was generated.");
        }
    }

    @Override
    public List<String> generateOverallRecommendations(AnalysisResult analysisResult,
                                                       AnalysisSummaryResponse summary,
                                                       List<DetectedIssue> issues) {
        String provider = validateConfiguration();

        try {
            String prompt = buildOverallRecommendationsPrompt(analysisResult, summary, issues);
            if ("gemini".equalsIgnoreCase(provider)) {
                return generateRecommendationsWithGemini(prompt);
            }
            return generateRecommendationsWithOpenRouter(prompt);
        } catch (HttpClientErrorException exception) {
            log.warn("{} client error while generating overall recommendations for analysis {}", providerLabel(provider), analysisResult.getId(), exception);
            throw new IllegalStateException(providerLabel(provider) + " rejected the request: HTTP " + exception.getStatusCode().value() + ".");
        } catch (HttpServerErrorException exception) {
            log.warn("{} server error while generating overall recommendations for analysis {}", providerLabel(provider), analysisResult.getId(), exception);
            throw new IllegalStateException(providerLabel(provider) + " service is temporarily unavailable: HTTP " + exception.getStatusCode().value() + ".");
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("{} failed to generate overall recommendations for analysis {}", providerLabel(provider), analysisResult.getId(), exception);
            throw new IllegalStateException(providerLabel(provider) + " request failed before AI recommendations were generated.");
        }
    }

    private String validateConfiguration() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("AI recommendations are disabled. Set AI_ENABLED=true.");
        }

        String provider = properties.getProvider();
        if (!hasText(provider)) {
            throw new IllegalStateException("AI provider is missing.");
        }
        if ("gemini".equalsIgnoreCase(provider)) {
            validateGeminiConfiguration();
            return provider;
        }
        if ("openrouter".equalsIgnoreCase(provider)) {
            validateOpenRouterConfiguration();
            return provider;
        }
        throw new IllegalStateException("Unsupported AI provider: " + provider + ".");
    }

    private void validateGeminiConfiguration() {
        if (properties.getGemini() == null) {
            throw new IllegalStateException("Gemini configuration is missing.");
        }
        if (!hasText(properties.getGemini().getApiKey())) {
            throw new IllegalStateException("Gemini API key is missing.");
        }
        if (!hasText(properties.getGemini().getModel())) {
            throw new IllegalStateException("Gemini model is missing.");
        }
        if (!hasText(properties.getGemini().getEndpoint())) {
            throw new IllegalStateException("Gemini endpoint is missing.");
        }
    }

    private void validateOpenRouterConfiguration() {
        if (properties.getOpenrouter() == null) {
            throw new IllegalStateException("OpenRouter configuration is missing.");
        }
        if (!hasText(properties.getOpenrouter().getApiKey())) {
            throw new IllegalStateException("OpenRouter API key is missing.");
        }
        if (!hasText(properties.getOpenrouter().getModel())) {
            throw new IllegalStateException("OpenRouter model is missing.");
        }
        if (!hasText(properties.getOpenrouter().getEndpoint())) {
            throw new IllegalStateException("OpenRouter endpoint is missing.");
        }
    }

    private String generateWithGemini(String prompt) {
        Map<String, Object> requestBody = buildGeminiRequestBody(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String endpoint = properties.getGemini().getEndpoint();
        String model = properties.getGemini().getModel();
        String apiKey = properties.getGemini().getApiKey();
        String url = "%s/%s:generateContent?key=%s".formatted(endpoint, model, apiKey);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
        return extractGeminiRecommendation(response.getBody());
    }

    private String generateWithOpenRouter(String prompt) {
        Map<String, Object> requestBody = buildOpenRouterRequestBody(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getOpenrouter().getApiKey());
        headers.add("X-Title", hasText(properties.getOpenrouter().getAppName()) ? properties.getOpenrouter().getAppName() : "AI Log Analyzer");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                properties.getOpenrouter().getEndpoint(),
                HttpMethod.POST,
                entity,
                JsonNode.class
        );
        return extractOpenRouterRecommendation(response.getBody());
    }

    private List<String> generateRecommendationsWithGemini(String prompt) {
        Map<String, Object> requestBody = buildGeminiRequestBody(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String endpoint = properties.getGemini().getEndpoint();
        String model = properties.getGemini().getModel();
        String apiKey = properties.getGemini().getApiKey();
        String url = "%s/%s:generateContent?key=%s".formatted(endpoint, model, apiKey);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
        return extractGeminiRecommendations(response.getBody());
    }

    private List<String> generateRecommendationsWithOpenRouter(String prompt) {
        Map<String, Object> requestBody = buildOpenRouterRequestBody(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getOpenrouter().getApiKey());
        headers.add("X-Title", hasText(properties.getOpenrouter().getAppName()) ? properties.getOpenrouter().getAppName() : "AI Log Analyzer");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                properties.getOpenrouter().getEndpoint(),
                HttpMethod.POST,
                entity,
                JsonNode.class
        );
        return extractOpenRouterRecommendations(response.getBody());
    }

    private Map<String, Object> buildGeminiRequestBody(String prompt) {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of(
                "role", "user",
                "parts", List.of(textPart)
        );

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("responseMimeType", "application/json");

        return Map.of(
                "contents", List.of(content),
                "generationConfig", generationConfig
        );
    }

    private Map<String, Object> buildOpenRouterRequestBody(String prompt) {
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.getOpenrouter().getModel());
        request.put("messages", List.of(message));
        request.put("temperature", 0.2);
        return request;
    }

    private String buildPrompt(AnalysisResult analysisResult, DetectedIssue issue) {
        return String.join("\n",
                "You are generating an AI recommendation for a production log analysis UI.",
                "Use only the data provided below.",
                "Do not invent systems, incidents, or configuration details that are not in evidence.",
                "If the evidence is weak, say so briefly and keep the recommendation cautious.",
                "Return strict JSON with one field: {\"aiRecommendation\":\"...\"}",
                "Keep the recommendation between 1 and 3 sentences and make it operational.",
                "",
                "Analysis context:",
                "- Primary root cause: %s",
                "- Summary: %s",
                "",
                "Finding:",
                "- Issue type: %s",
                "- Service: %s",
                "- Severity: %s",
                "- Frequency: %s",
                "- Confidence score: %.2f",
                "- Rule-based recommendation: %s",
                "- Evidence: %s"
        ).formatted(
                safe(analysisResult.getPrimaryRootCause()),
                safe(analysisResult.getSummaryText()),
                safe(issue.getIssueType()),
                safe(issue.getServiceName()),
                safe(issue.getSeverity() == null ? null : issue.getSeverity().name()),
                issue.getFrequency() == null ? "unknown" : issue.getFrequency().toString(),
                issue.getConfidenceScore() == null ? 0.0 : issue.getConfidenceScore(),
                safe(issue.getRecommendation()),
                safe(issue.getEvidenceText())
        );
    }

    private String buildOverallRecommendationsPrompt(AnalysisResult analysisResult,
                                                     AnalysisSummaryResponse summary,
                                                     List<DetectedIssue> issues) {
        return String.join("\n",
                "You are generating AI recommendations for a production log analysis recommendations panel.",
                "Use only the structured data provided below.",
                "Do not invent incidents, systems, or remediation steps that are unsupported by the evidence.",
                "Return strict JSON with one field: {\"aiRecommendations\":[\"...\",\"...\"]}",
                "Return 3 to 5 concise, operational next steps ordered by priority.",
                "",
                "Analysis context:",
                "- Primary root cause: %s",
                "- Summary: %s",
                "- Total logs: %d",
                "- Total errors: %d",
                "- Total warnings: %d",
                "",
                "Top recurring errors:",
                "%s",
                "",
                "Top failing services:",
                "%s",
                "",
                "Detected issues:",
                "%s",
                "",
                "Current deterministic recommendations:",
                "%s"
        ).formatted(
                safe(analysisResult.getPrimaryRootCause()),
                safe(analysisResult.getSummaryText()),
                summary.getTotalLogs(),
                summary.getTotalError(),
                summary.getTotalWarn(),
                summarizeTopCounts(summary.getTopRecurringErrors()),
                summarizeTopCounts(summary.getTopFailingServices()),
                summarizeIssues(issues),
                summarizeRecommendations(summary.getRecommendations())
        );
    }

    private String extractGeminiRecommendation(JsonNode body) {
        if (body == null) {
            throw new IllegalStateException("Gemini returned an empty response body.");
        }

        JsonNode textNode = body.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");
        if (!textNode.isTextual()) {
            throw new IllegalStateException("Gemini returned no recommendation text.");
        }

        try {
            JsonNode parsed = objectMapper.readTree(textNode.asText());
            JsonNode recommendationNode = parsed.path("aiRecommendation");
            if (!recommendationNode.isTextual()) {
                throw new IllegalStateException("Gemini returned invalid recommendation JSON.");
            }

            String recommendation = recommendationNode.asText().trim();
            if (recommendation.isEmpty()) {
                throw new IllegalStateException("Gemini returned an empty recommendation.");
            }
            return recommendation;
        } catch (Exception exception) {
            log.warn("Failed to parse Gemini JSON response", exception);
            throw new IllegalStateException("Gemini returned a response that could not be parsed.");
        }
    }

    private String extractOpenRouterRecommendation(JsonNode body) {
        if (body == null) {
            throw new IllegalStateException("OpenRouter returned an empty response body.");
        }

        JsonNode contentNode = body.path("choices")
                .path(0)
                .path("message")
                .path("content");
        if (!contentNode.isTextual()) {
            throw new IllegalStateException("OpenRouter returned no recommendation text.");
        }

        String content = contentNode.asText().trim();
        if (content.isEmpty()) {
            throw new IllegalStateException("OpenRouter returned an empty recommendation.");
        }

        try {
            JsonNode parsed = objectMapper.readTree(content);
            JsonNode recommendationNode = parsed.path("aiRecommendation");
            if (recommendationNode.isTextual() && hasText(recommendationNode.asText())) {
                return recommendationNode.asText().trim();
            }
        } catch (Exception ignored) {
            // Some free models ignore JSON-only instructions. Fall back to plain text.
        }

        return sanitizeTextRecommendation(content);
    }

    private List<String> extractGeminiRecommendations(JsonNode body) {
        if (body == null) {
            throw new IllegalStateException("Gemini returned an empty response body.");
        }

        JsonNode textNode = body.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");
        if (!textNode.isTextual()) {
            throw new IllegalStateException("Gemini returned no recommendations text.");
        }

        try {
            JsonNode parsed = objectMapper.readTree(textNode.asText());
            return parseRecommendationList(parsed.path("aiRecommendations"), "Gemini");
        } catch (Exception exception) {
            log.warn("Failed to parse Gemini recommendations JSON response", exception);
            throw new IllegalStateException("Gemini returned a response that could not be parsed.");
        }
    }

    private List<String> extractOpenRouterRecommendations(JsonNode body) {
        if (body == null) {
            throw new IllegalStateException("OpenRouter returned an empty response body.");
        }

        JsonNode contentNode = body.path("choices")
                .path(0)
                .path("message")
                .path("content");
        if (!contentNode.isTextual()) {
            throw new IllegalStateException("OpenRouter returned no recommendations text.");
        }

        String content = contentNode.asText().trim();
        if (content.isEmpty()) {
            throw new IllegalStateException("OpenRouter returned empty AI recommendations.");
        }

        try {
            JsonNode parsed = objectMapper.readTree(content);
            return parseRecommendationList(parsed.path("aiRecommendations"), "OpenRouter");
        } catch (Exception ignored) {
            return splitPlainTextRecommendations(content);
        }
    }

    private List<String> parseRecommendationList(JsonNode recommendationsNode, String providerLabel) {
        if (!recommendationsNode.isArray()) {
            throw new IllegalStateException(providerLabel + " returned invalid recommendations JSON.");
        }
        List<String> recommendations = new java.util.ArrayList<>();
        recommendationsNode.forEach(node -> {
            if (node.isTextual() && hasText(node.asText())) {
                recommendations.add(node.asText().trim());
            }
        });
        if (recommendations.isEmpty()) {
            throw new IllegalStateException(providerLabel + " returned empty AI recommendations.");
        }
        return recommendations;
    }

    private List<String> splitPlainTextRecommendations(String content) {
        List<String> recommendations = content.lines()
                .map(String::trim)
                .map(line -> line.replaceFirst("^[-*\\d.\\s]+", "").trim())
                .filter(this::hasText)
                .collect(Collectors.toList());
        if (recommendations.isEmpty()) {
            throw new IllegalStateException("OpenRouter returned empty AI recommendations.");
        }
        return recommendations.stream().limit(5).toList();
    }

    private String sanitizeTextRecommendation(String recommendation) {
        String sanitized = recommendation
                .replace("```json", "")
                .replace("```", "")
                .trim();
        if (sanitized.startsWith("{")) {
            throw new IllegalStateException("OpenRouter returned invalid recommendation JSON.");
        }
        return sanitized;
    }

    private String summarizeTopCounts(List<TopCountDto> counts) {
        if (counts == null || counts.isEmpty()) {
            return "- none";
        }
        return counts.stream()
                .limit(5)
                .map(item -> "- " + safe(item.getName()) + ": " + item.getCount())
                .collect(Collectors.joining("\n"));
    }

    private String summarizeIssues(List<DetectedIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "- none";
        }
        return issues.stream()
                .limit(5)
                .map(issue -> "- " + safe(issue.getIssueType()) + " | service=" + safe(issue.getServiceName()) + " | severity=" + (issue.getSeverity() == null ? "unknown" : issue.getSeverity().name()) + " | frequency=" + issue.getFrequency())
                .collect(Collectors.joining("\n"));
    }

    private String summarizeRecommendations(List<String> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "- none";
        }
        return recommendations.stream()
                .limit(5)
                .map(item -> "- " + item)
                .collect(Collectors.joining("\n"));
    }

    private int resolveTimeoutSeconds(AiProperties properties) {
        String provider = properties.getProvider();
        if ("openrouter".equalsIgnoreCase(provider) && properties.getOpenrouter() != null) {
            return properties.getOpenrouter().getTimeoutSeconds();
        }
        return properties.getGemini() == null ? 10 : properties.getGemini().getTimeoutSeconds();
    }

    private String providerLabel(String provider) {
        if ("openrouter".equalsIgnoreCase(provider)) {
            return "OpenRouter";
        }
        return "Gemini";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return hasText(value) ? value : "Not available";
    }
}
