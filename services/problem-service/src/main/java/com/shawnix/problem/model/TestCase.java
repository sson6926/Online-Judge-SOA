package com.shawnix.problem.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "testcases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // Object keys in MinIO (e.g. "problems/{problemId}/in_1.txt")
    @Column(name = "input_key", length = 512)
    private String inputKey;

    @Column(name = "output_key", length = 512)
    private String outputKey;
}
