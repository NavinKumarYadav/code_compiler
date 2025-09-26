package com.compiler.repository;

import com.compiler.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository  extends JpaRepository<Submission, Long> {
}
