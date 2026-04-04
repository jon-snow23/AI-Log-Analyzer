package com.example.loganalyzer.repository;

import com.example.loganalyzer.model.entity.DetectedIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetectedIssueRepository extends JpaRepository<DetectedIssue, Long> {
    List<DetectedIssue> findByAnalysisResultIdOrderByFrequencyDesc(Long analysisResultId);
    java.util.Optional<DetectedIssue> findByIdAndAnalysisResultId(Long id, Long analysisResultId);
}
