package com.compiler.service;

import com.compiler.entity.CodeSubmission;
import com.compiler.entity.User;
import com.compiler.repository.CodeSubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionHistoryService {

    private final CodeSubmissionRepository submissionRepository;

    public SubmissionHistoryService(CodeSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public CodeSubmission saveSubmission(CodeSubmission submission) {
        return submissionRepository.save(submission);
    }

    public CodeSubmission saveSubmissionWithResult(String code, String language, String output,
                                                   String error, String status, Double executionTime,
                                                   Double memoryUsed, Boolean isCorrect, User user, String sessionId) {

        CodeSubmission submission = new CodeSubmission();
        submission.setCode(code);
        submission.setLanguage(language);
        submission.setOutput(output);
        submission.setError(error);
        submission.setStatus(status);
        submission.setExecutionTime(executionTime);
        submission.setMemoryUsed(memoryUsed);
        submission.setIsCorrect(isCorrect);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setUser(user);
        submission.setSessionId(sessionId);

        return submissionRepository.save(submission);
    }

    public Optional<CodeSubmission> getSubmissionById(Long id) {
        return submissionRepository.findById(id);
    }

    public Page<CodeSubmission> getUserSubmissions(User user, Pageable pageable) {
        return submissionRepository.findByUserOrderBySubmittedAtDesc(user, pageable);
    }

    public Page<CodeSubmission> getAnonymousSubmissions(String sessionId, Pageable pageable) {
        return submissionRepository.findBySessionIdOrderBySubmittedAtDesc(sessionId, pageable);
    }
    public List<CodeSubmission> getRecentSubmissions() {
        return submissionRepository.findTop10ByOrderBySubmittedAtDesc();
    }

    public Page<CodeSubmission> getSubmissionsByLanguage(String language, Pageable pageable) {
        return submissionRepository.findByLanguageOrderBySubmittedAtDesc(language, pageable);
    }

    public Page<CodeSubmission> getSubmissionsByStatus(String status, Pageable pageable) {
        return submissionRepository.findByStatusOrderBySubmittedAtDesc(status, pageable);
    }

    public UserStatistics getUserStatistics(User user) {
        Long totalSubmissions = submissionRepository.countByUser(user);
        Long successfulSubmissions = submissionRepository.countByUserAndStatus(user, "Accepted");

        return new UserStatistics(totalSubmissions, successfulSubmissions);
    }

    public boolean deleteSubmission(Long submissionId, User user) {
        Optional<CodeSubmission> submission = submissionRepository.findById(submissionId);
        if (submission.isPresent() && submission.get().getUser() != null &&
                submission.get().getUser().getId().equals(user.getId())) {
            submissionRepository.delete(submission.get());
            return true;
        }
        return false;
    }

    public static class UserStatistics {
        private final Long totalSubmissions;
        private final Long successfulSubmissions;
        private final Double successRate;

        public UserStatistics(Long totalSubmissions, Long successfulSubmissions) {
            this.totalSubmissions = totalSubmissions;
            this.successfulSubmissions = successfulSubmissions;
            this.successRate = totalSubmissions > 0 ?
                    (double) successfulSubmissions / totalSubmissions * 100 : 0.0;
        }

        public Long getTotalSubmissions() {
            return totalSubmissions;
        }
        public Long getSuccessfulSubmissions() {
            return successfulSubmissions;
        }
        public Double getSuccessRate() {
            return successRate;
        }
    }
}