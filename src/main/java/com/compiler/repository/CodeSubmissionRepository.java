package com.compiler.repository;

import com.compiler.entity.CodeSubmission;
import com.compiler.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {

    Page<CodeSubmission> findByUserOrderBySubmittedAtDesc(User user, Pageable pageable);

    Page<CodeSubmission> findBySessionIdOrderBySubmittedAtDesc(String sessionId, Pageable pageable);

    List<CodeSubmission> findTop10ByOrderBySubmittedAtDesc();

    Page<CodeSubmission> findByLanguageOrderBySubmittedAtDesc(String language, Pageable pageable);

    Page<CodeSubmission> findByStatusOrderBySubmittedAtDesc(String status, Pageable pageable);

    Long countByUser(User user);

    Long countByUserAndStatus(User user, String status);

    @Query("SELECT cs FROM CodeSubmission cs WHERE cs.submittedAt BETWEEN :startDate AND :endDate")
    List<CodeSubmission> findBySubmittedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
}