package com.compiler.controller;

import com.compiler.entity.Problem;
import com.compiler.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
