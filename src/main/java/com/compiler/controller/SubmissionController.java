package com.compiler.controller;

import com.compiler.entity.CodeSubmission;
import com.compiler.service.SubmissionHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionHistoryService submissionHistoryService;

    public SubmissionController(SubmissionHistoryService submissionHistoryService) {
        this.submissionHistoryService = submissionHistoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeSubmission> getSubmission(@PathVariable Long id) {
        return submissionHistoryService.getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public List<CodeSubmission> getRecentSubmissions() {
        return submissionHistoryService.getRecentSubmissions();
    }

    @GetMapping("/my-submissions")
    public Page<CodeSubmission> getMySubmissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());

        String sessionId = "user_" + (userDetails != null ? userDetails.getUsername() : "anonymous");
        return submissionHistoryService.getAnonymousSubmissions(sessionId, pageable);
    }

    @GetMapping("/language/{language}")
    public Page<CodeSubmission> getSubmissionsByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return submissionHistoryService.getSubmissionsByLanguage(language, pageable);
    }

    @GetMapping("/status/{status}")
    public Page<CodeSubmission> getSubmissionsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return submissionHistoryService.getSubmissionsByStatus(status, pageable);
    }

    @GetMapping("/statistics")
    public ResponseEntity<SubmissionHistoryService.UserStatistics> getUserStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }

        SubmissionHistoryService.UserStatistics stats =
                new SubmissionHistoryService.UserStatistics(0L, 0L);
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubmission(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<CodeSubmission> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());

        return submissionHistoryService.getAnonymousSubmissions("all", pageable);
    }
}