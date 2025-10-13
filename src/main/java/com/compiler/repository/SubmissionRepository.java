package com.compiler.repository;

import com.compiler.entity.Submission;
import com.compiler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUser(User user);
    List<Submission> findByLanguage(String language);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.language = :language")
    List<Submission> findByUserAndLanguage(@Param("userId") Long userId,
                                           @Param("language") String language);

    List<Submission> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Submission> findByLanguageAndUserId(String language, Long userId);
}