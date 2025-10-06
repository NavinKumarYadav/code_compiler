package com.compiler.service;

import com.compiler.dto.SubmissionRequest;
import com.compiler.entity.Problem;
import com.compiler.entity.Submission;
import com.compiler.entity.SubmissionStatus;
import com.compiler.entity.User;
import com.compiler.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private UserService userService;

    @Value("${judge0.api.key}")
    private String rapidApiKey;

    private final String JUDGE0_URL = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=false&wait=true";

    public Submission submitCode(String username, SubmissionRequest submissionRequest) {
        User user = userService.findByUsername(username);
        Problem problem = problemService.getById(submissionRequest.getProblemId());

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setSourceCode(submissionRequest.getCode());
        submission.setLanguage(submissionRequest.getLanguage());
        submission.setStatus(SubmissionStatus.PENDING);
        submissionRepository.save(submission);

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
        if(statusDescription.contains("Time Limit Exceeded")){
            submission.setStatus( SubmissionStatus.TIMEOUT);
        } else if (statusDescription.contains("Accepted")) {
            submission.setStatus(SubmissionStatus.COMPLETED);
        }else {
            submission.setStatus(SubmissionStatus.FAILED);
        }
        submission.setOutput(output);

        return submissionRepository.save(submission);
    }
    public Submission getSubmission(Long id){
        return submissionRepository.findById(id)
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
