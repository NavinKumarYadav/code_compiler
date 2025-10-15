package com.compiler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "problems", uniqueConstraints = { @UniqueConstraint(columnNames = "title")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    private Integer timeLimitMs;
    private Integer memoryLimitKb;

    private String difficulty;

}
