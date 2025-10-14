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
                .orElseThrow(() -> new RuntimeException("Problem not found"));
    }
    public Problem create(Problem p){

        return problemRepository.save(p);
    }

}
