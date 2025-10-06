package com.compiler.service;

import com.compiler.dto.SubmissionRequest;
import com.compiler.entity.Problem;
import com.compiler.entity.Submission;
import com.compiler.entity.User;
import com.compiler.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private UserService userService;
    @Autowired
    private Judge0Service judge0Service;

    public Submission submitCode(String username, SubmissionRequest submissionRequest){
        User u=userService.findByUsername(username);
        Problem p=problemService.getById(submissionRequest.getProblemId());

        Submission submission = new Submission();
        submission.setUser(u);
        submission.setLanguage(submissionRequest.getLanguage());
        submission.setSourceCode(submissionRequest.getCode());
        submission.setStatus("PENDING");
        submissionRepository.save(submission);

        Map<String, Object>result=judge0Service.execute(submissionRequest.getCode(), submissionRequest.getLanguage());
        submission.setStatus((String) result.get("status").toString());
        submission.setRuntimeMs((Integer) result.get("time"));
        submission.setMemoryKb((Integer) result.get("memory"));
        return submissionRepository.save(submission);
    }

    public Submission getSubmission(Long id){
        return submissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

}
