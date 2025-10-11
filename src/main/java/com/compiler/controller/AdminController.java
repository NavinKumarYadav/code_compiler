package com.compiler.controller;

import com.compiler.entity.CodeSubmission;
import com.compiler.service.SubmissionHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SubmissionHistoryService submissionHistoryService;

    public AdminController(SubmissionHistoryService submissionHistoryService) {
        this.submissionHistoryService = submissionHistoryService;
    }

    @GetMapping("/submissions")
    public Page<CodeSubmission> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return submissionHistoryService.getAnonymousSubmissions("all", pageable);
    }

    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<?> deleteSubmission(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getPlatformStatistics() {
        return ResponseEntity.ok().build();
    }
}