package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.SlowQueryDTO;
import com.lsi.oracle.Service.PerformanceOptimizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;





@RestController
@RequestMapping("/api/performance-optimization")
@Slf4j
public class PerformanceOptimizationController {

  @Autowired
  private PerformanceOptimizationService optimizationService;

  @PostMapping("/params/slow-queries-trash/{number}")
  public ResponseEntity<Long> getSlowQueriesTrash(@PathVariable Long number) {
    if (number < 0 || number == null){
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    optimizationService.setSlowQueryThreshold(number);
    return ResponseEntity.ok(optimizationService.getSlowQueryThreshold());
  }

  @GetMapping("/slow-queries")
  public ResponseEntity<List<SlowQueryDTO>> fetchSlowQueries() {
    try {
      List<SlowQueryDTO> queries = optimizationService.fetchSlowQueries();
      return ResponseEntity.ok(queries);
    } catch (Exception e) {
      log.error("Error fetching slow queries", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/tune-query/{sqlId}")
  public ResponseEntity<Map<String, Object>> tuneQuery(@PathVariable("sqlId") String sqlId) {
    try {
      if (sqlId == null || sqlId.trim().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "SQL ID cannot be null or empty"));
      }

      Map<String, Object> result = optimizationService.tuneQuery(sqlId);

      if (result == null || result.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "No tuning result found for SQL ID: " + sqlId));
      }

      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Error tuning query for SQL ID: {}", sqlId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Failed to tune query: " + e.getMessage()));
    }
  }


  @PostMapping("/schedule-stats/{frequency}")
  public ResponseEntity<Map<String, String>> scheduleStatsRecalculation(
    @PathVariable("frequency") String frequency) {
    try {
      optimizationService.scheduleStatsRecalculation(frequency);
      return ResponseEntity.ok(Map.of(
        "message", "Statistics recalculation scheduled successfully for frequency: " + frequency,
        "frequency", frequency
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Error scheduling stats recalculation", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Failed to schedule statistics recalculation: " + e.getMessage()));
    }
  }

  @GetMapping("/jobs")
  public ResponseEntity<?> jobs(){
    return ResponseEntity.ok(optimizationService.getAllJobs());
  }


}
