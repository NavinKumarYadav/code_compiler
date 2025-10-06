package com.compiler.controller;

import com.compiler.dto.SubmissionRequest;
import com.compiler.dto.SubmissionResponse;
import com.compiler.entity.Submission;
import com.compiler.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService service;

    @PostMapping
    public SubmissionResponse submit(@AuthenticationPrincipal UserDetails user,
                                     @RequestBody SubmissionRequest request){
        Submission s = service.submitCode(user.getUsername(), request);
        return mapToResponse(s);
    }

    @GetMapping("/{id}")
    public SubmissionResponse get(@PathVariable Long id){
        Submission s = service.getSubmission(id);
        return mapToResponse(s);
    }
    private SubmissionResponse mapToResponse(Submission s){
        return new SubmissionResponse(
                s.getId(), s.getStatus(),s.getRuntimeMs(),s.getMemoryKb()
        );
    }
}
