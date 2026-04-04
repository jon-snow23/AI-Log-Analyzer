package com.example.loganalyzer.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_log_id", unique = true)
    private UploadedLog uploadedLog;

    @Column(nullable = false)
    private Integer totalLogs;

    @Column(nullable = false)
    private Integer totalInfo;

    @Column(nullable = false)
    private Integer totalWarn;

    @Column(nullable = false)
    private Integer totalError;

    private String primaryRootCause;

    private Double confidenceScore;

    @Column(length = 5000)
    private String summaryText;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
