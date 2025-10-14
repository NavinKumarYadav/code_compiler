package com.compiler.controller;

import com.compiler.entity.CodeSubmission;
import com.compiler.service.SubmissionHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SubmissionHistoryService submissionHistoryService;

    public AdminController(SubmissionHistoryService submissionHistoryService) {
        this.submissionHistoryService = submissionHistoryService;
    }

    @GetMapping("/submissions")
    public ResponseEntity<?> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
            Page<CodeSubmission> submissions = submissionHistoryService.getAllSubmissions(pageable);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/submissions/debug")
    public ResponseEntity<?> debugSubmissions() {
        try {
            long total = submissionHistoryService.getTotalSubmissions();
            return ResponseEntity.ok(Map.of(
                    "totalInDatabase", total,
                    "message", total > 0 ? "Data exists" : "No data"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<?> deleteSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", "Delete endpoint ready"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getPlatformStatistics() {
        return ResponseEntity.ok(Map.of("message", "Statistics endpoint ready"));
    }
}