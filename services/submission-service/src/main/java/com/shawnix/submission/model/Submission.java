package com.shawnix.submission.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "problem_id", nullable = false)
    private UUID problemId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Column(nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Verdict verdict;

    @Column(name = "time_used")
    private Integer timeUsed;

    @Column(name = "memory_used")
    private Integer memoryUsed;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public enum Status {
        PENDING, RUNNING, FINISHED
    }

    public enum Verdict {
        AC, WA, TLE, MLE, RTE, CE, SE
    }
}
