package com.compiler.service;

import com.compiler.dto.SubmissionRequest;
import com.compiler.entity.*;
import com.compiler.repository.CodeSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubmissionService {

   @Autowired
   private CodeSubmissionRepository codeSubmissionRepository;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private UserService userService;

    @Value("${judge0.api.key}")
    private String rapidApiKey;

    private final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";

    public CodeSubmission submitCode(String username, SubmissionRequest submissionRequest) {
        User user = userService.findByUsername(username);
        Problem problem = problemService.getById(submissionRequest.getProblemId());

        CodeSubmission submission = new CodeSubmission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setCode(submissionRequest.getCode());
        submission.setLanguage(submissionRequest.getLanguage());
        submission.setStatus("PENDING");
        submission.setSubmittedAt(LocalDateTime.now());
        codeSubmissionRepository.save(submission);

        Map<String, Object> body = new HashMap<>();
        body.put("source_code", submissionRequest.getCode());
        body.put("language_id", getLanguageId(submissionRequest.getLanguage()));
        body.put("stdin", problem.getInput() !=null ? problem.getInput() : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", rapidApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(JUDGE0_URL, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        String output = responseBody.get("stdout") != null ? responseBody.get("stdout").toString() :
                responseBody.get("message") != null ? responseBody.get("message").toString() : "Error";

        String statusDescription = responseBody.get("status") != null &&
                ((Map) responseBody.get("status")).get("description") != null
                ? ((Map) responseBody.get("status")).get("description").toString() : "";

        String status;
        if(statusDescription.contains("Time Limit Exceeded")){
            status = "TIMEOUT";
        } else if (statusDescription.contains("Accepted")) {
            status = "COMPLETED";
        } else {
            status = "FAILED";
        }
        submission.setStatus(status);
        submission.setOutput(output);

        return codeSubmissionRepository.save(submission);
    }
    public CodeSubmission getSubmission(Long id){
        return codeSubmissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    private int getLanguageId(String language){
        return switch (language.toLowerCase()){
            case "c" -> 50;
            case "cpp" -> 54;
            case "java" -> 62;
            case "python3" -> 71;
            case "javascript" -> 63;
            default -> 62;
        };
    }

}
