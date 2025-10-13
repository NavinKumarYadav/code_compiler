package com.compiler.controller;

import com.compiler.entity.CodeSubmission;
import com.compiler.service.SubmissionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Submissions", description = "APIs for managing code submission history")
public class SubmissionController {

    private final SubmissionHistoryService submissionHistoryService;

    public SubmissionController(SubmissionHistoryService submissionHistoryService) {
        this.submissionHistoryService = submissionHistoryService;
    }

    @Operation(
            summary = "Get submission by ID",
            description = "Retrieve a specific code submission by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Submission found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CodeSubmission.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission not found with the provided ID"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CodeSubmission> getSubmission(
            @Parameter(description = "Submission ID", example = "1", required = true)
            @PathVariable Long id) {
        return submissionHistoryService.getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get recent submissions",
            description = "Retrieve the 10 most recent code submissions from all users"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Recent submissions retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CodeSubmission[].class))
    )
    @GetMapping("/recent")
    public List<CodeSubmission> getRecentSubmissions() {
        return submissionHistoryService.getRecentSubmissions();
    }

    @Operation(
            summary = "Get user's submissions",
            description = "Retrieve paginated code submissions for the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User submissions retrieved successfully"
    )
    @GetMapping("/my-submissions")
    public Page<CodeSubmission> getMySubmissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());

        String sessionId = "user_" + (userDetails != null ? userDetails.getUsername() : "anonymous");
        return submissionHistoryService.getAnonymousSubmissions(sessionId, pageable);
    }

    @Operation(
            summary = "Get submissions by language",
            description = "Retrieve paginated code submissions filtered by programming language"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Language-specific submissions retrieved successfully"
    )
    @GetMapping("/language/{language}")
    public Page<CodeSubmission> getSubmissionsByLanguage(
            @Parameter(description = "Programming language", example = "java", required = true)
            @PathVariable String language,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return submissionHistoryService.getSubmissionsByLanguage(language, pageable);
    }

    @Operation(
            summary = "Get submissions by status",
            description = "Retrieve paginated code submissions filtered by execution status"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Status-specific submissions retrieved successfully"
    )
    @GetMapping("/status/{status}")
    public Page<CodeSubmission> getSubmissionsByStatus(
            @Parameter(description = "Execution status", example = "Accepted", required = true)
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return submissionHistoryService.getSubmissionsByStatus(status, pageable);
    }

    @Operation(
            summary = "Get user statistics",
            description = "Retrieve statistics for the authenticated user (total submissions, success rate)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User statistics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User not authenticated"
            )
    })
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

    @Operation(
            summary = "Delete submission",
            description = "Delete a specific code submission (requires authentication)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Submission deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubmission(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Submission ID", example = "1", required = true)
            @PathVariable Long id) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().build();
    }
}