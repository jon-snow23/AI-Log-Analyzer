package com.example.loganalyzer.service;

import com.example.loganalyzer.analyzer.AnalysisComputation;
import com.example.loganalyzer.analyzer.LogAnalysisEngine;
import com.example.loganalyzer.dto.*;
import com.example.loganalyzer.exception.BadRequestException;
import com.example.loganalyzer.exception.ResourceNotFoundException;
import com.example.loganalyzer.mapper.AnalysisMapper;
import com.example.loganalyzer.model.entity.*;
import com.example.loganalyzer.model.enums.SourceType;
import com.example.loganalyzer.model.enums.UploadStatus;
import com.example.loganalyzer.parser.LogParser;
import com.example.loganalyzer.parser.ParsedLogLine;
import com.example.loganalyzer.repository.AnalysisResultRepository;
import com.example.loganalyzer.repository.DetectedIssueRepository;
import com.example.loganalyzer.repository.LogEntryRepository;
import com.example.loganalyzer.repository.UploadedLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);

    private final UploadedLogRepository uploadedLogRepository;
    private final LogEntryRepository logEntryRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final DetectedIssueRepository detectedIssueRepository;
    private final LogParser logParser;
    private final LogAnalysisEngine analysisEngine;
    private final AnalysisMapper analysisMapper;
    private final AiRecommendationService aiRecommendationService;

    public LogAnalysisService(UploadedLogRepository uploadedLogRepository,
                              LogEntryRepository logEntryRepository,
                              AnalysisResultRepository analysisResultRepository,
                              DetectedIssueRepository detectedIssueRepository,
                              LogParser logParser,
                              LogAnalysisEngine analysisEngine,
                              AnalysisMapper analysisMapper,
                              AiRecommendationService aiRecommendationService) {
        this.uploadedLogRepository = uploadedLogRepository;
        this.logEntryRepository = logEntryRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.detectedIssueRepository = detectedIssueRepository;
        this.logParser = logParser;
        this.analysisEngine = analysisEngine;
        this.analysisMapper = analysisMapper;
        this.aiRecommendationService = aiRecommendationService;
    }

    @Transactional
    public AnalysisSummaryResponse analyzeUpload(MultipartFile file) {
        validateFile(file);
        try {
            String rawLogs = new String(file.getBytes(), StandardCharsets.UTF_8);
            return analyze(SourceType.FILE, Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded.log"), rawLogs);
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read uploaded file.");
        }
    }

    @Transactional
    public AnalysisSummaryResponse analyzeText(String rawLogs) {
        if (rawLogs == null || rawLogs.isBlank()) {
            throw new BadRequestException("Raw logs must not be blank.");
        }
        return analyze(SourceType.TEXT, "pasted-logs.txt", rawLogs);
    }

    @Transactional(readOnly = true)
    public AnalysisSummaryResponse getSummary(Long analysisId) {
        AnalysisResult result = getAnalysisResult(analysisId);
        UploadedLog uploadedLog = result.getUploadedLog();
        AnalysisComputation computation = analysisEngine.analyze(logEntryRepository.findAllByUploadedLogId(uploadedLog.getId()));
        return analysisMapper.toSummaryResponse(result, uploadedLog, computation.getTopErrors(), computation.getTopServices(), computation.getRecommendations());
    }

    @Transactional(readOnly = true)
    public List<DetectedIssueDto> getIssues(Long analysisId) {
        getAnalysisResult(analysisId);
        return detectedIssueRepository.findByAnalysisResultIdOrderByFrequencyDesc(analysisId).stream()
                .map(analysisMapper::toIssueDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AiRecommendationResponse generateIssueRecommendation(Long analysisId, Long issueId) {
        AnalysisResult result = getAnalysisResult(analysisId);
        DetectedIssue issue = detectedIssueRepository.findByIdAndAnalysisResultId(issueId, analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        String aiRecommendation;
        try {
            aiRecommendation = aiRecommendationService.generateIssueRecommendation(result, issue);
        } catch (IllegalStateException exception) {
            throw new BadRequestException(exception.getMessage());
        }

        return AiRecommendationResponse.builder()
                .analysisId(analysisId)
                .issueId(issueId)
                .aiRecommendation(aiRecommendation)
                .build();
    }

    @Transactional(readOnly = true)
    public AiRecommendationsResponse generateOverallRecommendations(Long analysisId) {
        AnalysisResult result = getAnalysisResult(analysisId);
        UploadedLog uploadedLog = result.getUploadedLog();
        AnalysisComputation computation = analysisEngine.analyze(logEntryRepository.findAllByUploadedLogId(uploadedLog.getId()));
        AnalysisSummaryResponse summary = analysisMapper.toSummaryResponse(result, uploadedLog, computation.getTopErrors(), computation.getTopServices(), computation.getRecommendations());
        List<DetectedIssue> issues = detectedIssueRepository.findByAnalysisResultIdOrderByFrequencyDesc(analysisId);

        List<String> aiRecommendations;
        try {
            aiRecommendations = aiRecommendationService.generateOverallRecommendations(result, summary, issues);
        } catch (IllegalStateException exception) {
            throw new BadRequestException(exception.getMessage());
        }

        return AiRecommendationsResponse.builder()
                .analysisId(analysisId)
                .recommendations(aiRecommendations)
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<LogEntryDto> getEntries(Long analysisId, String level, String service, String search, int page, int size) {
        AnalysisResult result = getAnalysisResult(analysisId);
        Specification<LogEntry> specification = byUploadedLog(result.getUploadedLog().getId())
                .and(matchesLevel(level))
                .and(matchesService(service))
                .and(matchesSearch(search));
        return analysisMapper.toPagedLogResponse(logEntryRepository.findAll(specification, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @Transactional(readOnly = true)
    public ExportResponse export(Long analysisId) {
        return ExportResponse.builder()
                .summary(getSummary(analysisId))
                .issues(getIssues(analysisId))
                .build();
    }

    private AnalysisSummaryResponse analyze(SourceType sourceType, String fileName, String rawLogs) {
        UploadedLog uploadedLog = uploadedLogRepository.save(UploadedLog.builder()
                .sourceType(sourceType)
                .fileName(fileName)
                .uploadTime(OffsetDateTime.now())
                .status(UploadStatus.PROCESSING)
                .build());

        List<ParsedLogLine> parsedLines = logParser.parse(rawLogs);
        if (parsedLines.isEmpty()) {
            throw new BadRequestException("No log lines were found to analyze.");
        }

        List<LogEntry> entries = parsedLines.stream().map(line -> LogEntry.builder()
                .uploadedLog(uploadedLog)
                .timestamp(line.getTimestamp())
                .level(line.getLevel())
                .serviceName(line.getServiceName())
                .message(line.getMessage())
                .exceptionType(line.getExceptionType())
                .rawLine(line.getRawLine())
                .normalizedMessage(line.getNormalizedMessage())
                .build()).toList();
        logEntryRepository.saveAll(entries);

        AnalysisComputation computation = analysisEngine.analyze(entries);
        AnalysisResult result = analysisResultRepository.save(AnalysisResult.builder()
                .uploadedLog(uploadedLog)
                .totalLogs(computation.getTotalLogs())
                .totalInfo(computation.getTotalInfo())
                .totalWarn(computation.getTotalWarn())
                .totalError(computation.getTotalError())
                .primaryRootCause(computation.getPrimaryCandidate().getIssueType())
                .confidenceScore(computation.getPrimaryCandidate().getConfidenceScore())
                .summaryText(computation.getSummaryText())
                .createdAt(OffsetDateTime.now())
                .build());

        detectedIssueRepository.saveAll(computation.getCandidates().stream()
                .map(candidate -> DetectedIssue.builder()
                        .analysisResult(result)
                        .issueType(candidate.getIssueType())
                        .serviceName(candidate.getServiceName())
                        .severity(candidate.getSeverity())
                        .frequency(candidate.getFrequency())
                        .recommendation(candidate.getRecommendation())
                        .evidenceText(candidate.getEvidence())
                        .confidenceScore(candidate.getConfidenceScore())
                        .build())
                .toList());

        uploadedLog.setStatus(UploadStatus.COMPLETED);
        uploadedLogRepository.save(uploadedLog);
        log.info("Completed analysis {} for upload {}", result.getId(), uploadedLog.getId());

        return analysisMapper.toSummaryResponse(result, uploadedLog, computation.getTopErrors(), computation.getTopServices(), computation.getRecommendations());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A .log or .txt file is required.");
        }
        String name = file.getOriginalFilename();
        if (name == null || (!name.endsWith(".log") && !name.endsWith(".txt"))) {
            throw new BadRequestException("Only .log and .txt files are supported.");
        }
    }

    private AnalysisResult getAnalysisResult(Long analysisId) {
        return analysisResultRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found: " + analysisId));
    }

    private Specification<LogEntry> byUploadedLog(Long uploadedLogId) {
        return (root, query, cb) -> cb.equal(root.get("uploadedLog").get("id"), uploadedLogId);
    }

    private Specification<LogEntry> matchesLevel(String level) {
        return (root, query, cb) -> level == null || level.isBlank() ? cb.conjunction() : cb.equal(cb.upper(root.get("level")), level.toUpperCase());
    }

    private Specification<LogEntry> matchesService(String service) {
        return (root, query, cb) -> service == null || service.isBlank() ? cb.conjunction() : cb.equal(root.get("serviceName"), service);
    }

    private Specification<LogEntry> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("message")), like),
                    cb.like(cb.lower(root.get("rawLine")), like),
                    cb.like(cb.lower(root.get("serviceName")), like)
            );
        };
    }
}
