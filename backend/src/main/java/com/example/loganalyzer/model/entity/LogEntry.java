package com.example.loganalyzer.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "log_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_log_id")
    private UploadedLog uploadedLog;

    private OffsetDateTime timestamp;

    @Column(length = 20)
    private String level;

    private String serviceName;

    @Column(length = 5000)
    private String message;

    private String exceptionType;

    @Column(nullable = false, length = 5000)
    private String rawLine;

    @Column(length = 2000)
    private String normalizedMessage;
}
