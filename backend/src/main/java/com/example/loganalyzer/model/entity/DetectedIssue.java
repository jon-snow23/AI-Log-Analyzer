package com.example.loganalyzer.model.entity;

import com.example.loganalyzer.model.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detected_issues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_result_id")
    private AnalysisResult analysisResult;

    @Column(nullable = false)
    private String issueType;

    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private Integer frequency;

    @Column(length = 2000)
    private String recommendation;

    @Column(length = 3000)
    private String evidenceText;

    @Column(nullable = false)
    private Double confidenceScore;
}
