package com.compiler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Problem problem;

    private String language;

    @Column(columnDefinition = "Text")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(length = 5000)
    private String output;

    private Integer runtimeMs;
    private Integer memoryKb;



}
