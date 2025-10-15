package com.compiler.service;

import com.compiler.entity.Problem;
import com.compiler.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {
    @Autowired
    private ProblemRepository problemRepository;

    public List<Problem> getAll(){
        return problemRepository.findAll();
    }

    public Problem getById(Long id){
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
    }

    public boolean existsByTitle(String title) {
        return problemRepository.findByTitle(title).isPresent();
    }

    public Problem create(Problem p){

            if (p.getTitle() == null || p.getTitle().trim().isEmpty()) {
                throw new RuntimeException("Problem title cannot be empty");
            }

            if (existsByTitle(p.getTitle())) {
                throw new RuntimeException("Problem with title '" + p.getTitle() + "' already exists");
            }

            if (p.getDescription() == null || p.getDescription().trim().isEmpty()) {
                throw new RuntimeException("Problem description cannot be empty");
            }

            if (p.getDifficulty() == null || p.getDifficulty().trim().isEmpty()) {
                throw new RuntimeException("Problem difficulty cannot be empty");
            }

            if (!isValidDifficulty(p.getDifficulty())) {
                throw new RuntimeException("Invalid difficulty. Must be EASY, MEDIUM, or HARD");
            }
            return problemRepository.save(p);

    }

    public Problem update(Long id, Problem problemDetails) {
        Problem existingProblem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));

        if (problemDetails.getTitle() != null &&
                !problemDetails.getTitle().equals(existingProblem.getTitle())) {
            if (existsByTitle(problemDetails.getTitle())) {
                throw new RuntimeException("Problem with title '" + problemDetails.getTitle() + "' already exists");
            }
            existingProblem.setTitle(problemDetails.getTitle());
        }

        if (problemDetails.getDescription() != null) {
            existingProblem.setDescription(problemDetails.getDescription());
        }
        if (problemDetails.getInput() != null) {
            existingProblem.setInput(problemDetails.getInput());
        }
        if (problemDetails.getOutput() != null) {
            existingProblem.setOutput(problemDetails.getOutput());
        }
        if (problemDetails.getTimeLimitMs() != null) {
            existingProblem.setTimeLimitMs(problemDetails.getTimeLimitMs());
        }
        if (problemDetails.getMemoryLimitKb() != null) {
            existingProblem.setMemoryLimitKb(problemDetails.getMemoryLimitKb());
        }

        if (problemDetails.getDifficulty() != null) {
            if (!isValidDifficulty(problemDetails.getDifficulty())) {
                throw new RuntimeException("Invalid difficulty. Must be EASY, MEDIUM, or HARD");
            }
            existingProblem.setDifficulty(problemDetails.getDifficulty());
        }

        return problemRepository.save(existingProblem);
    }

    public void delete(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new RuntimeException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    public List<Problem> findByDifficulty(String difficulty) {
        if (!isValidDifficulty(difficulty)) {
            throw new RuntimeException("Invalid difficulty. Must be EASY, MEDIUM, or HARD");
        }
        return problemRepository.findByDifficulty(difficulty);
    }

    public List<Problem> searchByTitle(String title) {
        return problemRepository.findByTitleContainingIgnoreCase(title);
    }

    public boolean existsById(Long id) {
        return problemRepository.existsById(id);
    }

    public long getCount() {
        return problemRepository.count();
    }

    private boolean isValidDifficulty(String difficulty) {
        return difficulty != null &&
                (difficulty.equalsIgnoreCase("EASY") ||
                        difficulty.equalsIgnoreCase("MEDIUM") ||
                        difficulty.equalsIgnoreCase("HARD"));
    }

    public List<Problem> findByDifficultyAndTitle(String difficulty, String title) {
        if (!isValidDifficulty(difficulty)) {
            throw new RuntimeException("Invalid difficulty. Must be EASY, MEDIUM, or HARD");
        }
        return problemRepository.findByDifficultyAndTitleContainingIgnoreCase(difficulty, title);
    }

    public long getCountByDifficulty(String difficulty) {
        if (!isValidDifficulty(difficulty)) {
            throw new RuntimeException("Invalid difficulty. Must be EASY, MEDIUM, or HARD");
        }
        return problemRepository.countByDifficulty(difficulty);
    }
}