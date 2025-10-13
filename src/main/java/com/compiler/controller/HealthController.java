package com.compiler.controller;


import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ApplicationAvailability applicationAvailability;

    public HealthController(ApplicationAvailability applicationAvailability){
        this.applicationAvailability = applicationAvailability;
    }

    @GetMapping
    public Map<String, Object> healthCheck() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "liveness", getState(applicationAvailability.getLivenessState()),
                "readiness", getState(applicationAvailability.getReadinessState()),
                "service", "Code Compiler API",
                "version", "1.0.0"
        );
    }

    private String getState(AvailabilityState state) {
        if (state == LivenessState.CORRECT) {
            return "CORRECT";
        } else if (state == ReadinessState.ACCEPTING_TRAFFIC) {
            return "ACCEPTING_TRAFFIC";
        }
        return state.toString();
    }

    @GetMapping("/readiness")
    public Map<String, String> readiness() {
        return Map.of("status", "READY");
    }

    @GetMapping("/liveness")
    public Map<String, String> liveness() {
        return Map.of("status", "LIVE");
    }
}
