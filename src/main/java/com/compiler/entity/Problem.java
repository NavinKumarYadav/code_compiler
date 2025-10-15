package com.compiler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "Text")
    private String description;

    @Column(columnDefinition = "Text")
    private String input;

    @Column(columnDefinition = "Text")
    private String output;

    private Integer timeLimitMs;
    private Integer memoryLimitKb;

    private String difficulty;

}
