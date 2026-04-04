package com.example.loganalyzer.repository;

import com.example.loganalyzer.model.entity.UploadedLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedLogRepository extends JpaRepository<UploadedLog, Long> {
}
