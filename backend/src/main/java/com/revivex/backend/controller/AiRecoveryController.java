package com.revivex.backend.controller;

import com.revivex.backend.entity.InterventionLog;
import com.revivex.backend.repository.InterventionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/recovery")
@RequiredArgsConstructor
public class AiRecoveryController {

    private final InterventionLogRepository interventionLogRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRecoveryStats() {
        List<InterventionLog> logs = interventionLogRepository.findAll();
        
        BigDecimal totalAtRisk = BigDecimal.ZERO;
        BigDecimal totalRecovered = BigDecimal.ZERO;
        int totalInterventions = logs.size();
        int recoveredCount = 0;

        for (InterventionLog log : logs) {
            totalAtRisk = totalAtRisk.add(log.getRevenueAtRisk());
            if ("RECOVERED".equals(log.getStatus())) {
                totalRecovered = totalRecovered.add(log.getRevenueAtRisk());
                recoveredCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAtRisk", totalAtRisk);
        stats.put("totalRecovered", totalRecovered);
        stats.put("totalInterventions", totalInterventions);
        stats.put("recoveredCount", recoveredCount);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<InterventionLog>> getRecoveryLogs() {
        return ResponseEntity.ok(interventionLogRepository.findAllByOrderByCreatedAtDesc());
    }
}
