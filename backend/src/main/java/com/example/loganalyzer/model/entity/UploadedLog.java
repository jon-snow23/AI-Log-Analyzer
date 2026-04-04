package com.example.loganalyzer.model.entity;

import com.example.loganalyzer.model.enums.SourceType;
import com.example.loganalyzer.model.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "uploaded_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    private String fileName;

    @Column(nullable = false)
    private OffsetDateTime uploadTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadStatus status;
}
