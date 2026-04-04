package com.shawnix.problem.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @Column(name = "memory_limit")
    private Integer memoryLimit;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
