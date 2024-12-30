package com.lsi.oracle.controller;

import com.lsi.oracle.Service.PerformanceOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance-optimization")
public class PerformanceOptimizationController {

  @Autowired
  private PerformanceOptimizationService optimizationService;

  @GetMapping("/slow-queries")
  public List<Map<String, Object>> fetchSlowQueries() {
    return optimizationService.fetchSlowQueries();
  }

  @PostMapping("/tune-query")
  public Map<String, Object> tuneQuery(@RequestParam String sqlId) {
    return optimizationService.tuneQuery(sqlId);
  }

  @PostMapping("/schedule-stats")
  public String scheduleStatsRecalculation(@RequestParam String frequency) {
    optimizationService.scheduleStatsRecalculation(frequency);
    return "Statistics recalculation scheduled successfully for frequency: " + frequency;
  }
}
