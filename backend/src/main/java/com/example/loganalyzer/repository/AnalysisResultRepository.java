package com.example.loganalyzer.repository;

import com.example.loganalyzer.model.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findByUploadedLogId(Long uploadedLogId);
}
