package com.example.loganalyzer.repository;

import com.example.loganalyzer.model.entity.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {
    Page<LogEntry> findByUploadedLogId(Long uploadedLogId, Pageable pageable);
    List<LogEntry> findAllByUploadedLogId(Long uploadedLogId);
}
