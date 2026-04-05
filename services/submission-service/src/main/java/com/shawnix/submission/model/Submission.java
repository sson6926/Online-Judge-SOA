package com.shawnix.submission.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "problem_id không được để trống")
    private UUID problemId;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "user_id không được để trống")
    private UUID userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "code không được để trống")
    private String code;

    @Column(nullable = false)
    @NotBlank(message = "language không được để trống")
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
