package com.compiler.repository;

import com.compiler.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem,Long> {

    Optional<Problem> findByTitle(String title);

    List<Problem> findByDifficulty(String difficulty);

    List<Problem> findByTitleContainingIgnoreCase(String title);

    List<Problem> findByDifficultyAndTitleContainingIgnoreCase(String difficulty, String title);

    long countByDifficulty(String difficulty);
}
