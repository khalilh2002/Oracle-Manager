package com.lsi.oracle.Controller;

import com.lsi.oracle.Service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@Slf4j
@CrossOrigin(origins = "*") // Adjust according to your security requirements
public class PerformanceController {

  private final PerformanceService performanceService;

  @Autowired
  public PerformanceController(PerformanceService performanceService) {
    this.performanceService = performanceService;
  }

  @GetMapping("/realtime")
  public ResponseEntity<List<Map<String, Object>>> getRealtimeMetrics() {
    try {
      return ResponseEntity.ok(performanceService.getRealtimeMetrics());
    } catch (Exception e) {
      log.error("Error fetching realtime metrics", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/current")
  public ResponseEntity<Map<String, Object>> getCurrentMetrics() {
    try {
      return ResponseEntity.ok(performanceService.getDetailedRealtimeStats());
    } catch (Exception e) {
      log.error("Error fetching current metrics", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/system")
  public ResponseEntity<Map<String, Object>> getSystemMetrics() {
    try {
      return ResponseEntity.ok(performanceService.getDetailedSystemMetrics());
    } catch (Exception e) {
      log.error("Error fetching system metrics", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/io")
  public ResponseEntity<Map<String, Object>> getIOStats() {
    try {
      return ResponseEntity.ok(performanceService.getIOStats());
    } catch (Exception e) {
      log.error("Error fetching IO statistics", e);
      return ResponseEntity.internalServerError().build();
    }
  }


}
