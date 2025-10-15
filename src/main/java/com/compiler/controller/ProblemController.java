package com.compiler.controller;

import com.compiler.entity.Problem;
import com.compiler.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {
    @Autowired
    private ProblemService service;

    @GetMapping
    public List<Problem> getAll(){

        return service.getAll();
    }

    @GetMapping("/{id}")
    public Problem getById(@PathVariable Long id){

        return service.getById(id);
    }

    @PostMapping
    public Problem create(@RequestBody Problem p){
        return service.create(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Problem deleted successfully",
                    "deletedId", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Problem not found with id: " + id
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProblem(@PathVariable Long id, @RequestBody Problem problemDetails) {
        try {
            Problem updatedProblem = service.update(id, problemDetails);
            return ResponseEntity.ok(updatedProblem);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<?> getByDifficulty(@PathVariable String difficulty) {
        try {
            List<Problem> problems = service.findByDifficulty(difficulty);
            return ResponseEntity.ok(problems);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "No problems found with difficulty: " + difficulty
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByTitle(@RequestParam String title) {
        try {
            List<Problem> problems = service.searchByTitle(title);
            return ResponseEntity.ok(problems);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "No problems found with title containing: " + title
            ));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getProblemsCount() {
        try {
            long count = service.getCount();
            return ResponseEntity.ok(Map.of(
                    "totalProblems", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to get problems count"
            ));
        }
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<?> problemExists(@PathVariable Long id) {
        try {
            boolean exists = service.existsById(id);
            return ResponseEntity.ok(Map.of(
                    "exists", exists
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to check problem existence"
            ));
        }
    }
}
