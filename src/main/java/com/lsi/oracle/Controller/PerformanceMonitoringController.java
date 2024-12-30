package com.lsi.oracle.controller;

import com.lsi.oracle.Service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance-monitoring")
public class PerformanceMonitoringController {

  @Autowired
  private PerformanceService performanceService;

  @GetMapping("/db-id")
  public String getDbId() {
    return performanceService.getDbId();
  }

  @GetMapping("/snapshots")
  public List<Map<String, Object>> getSnapshots() {
    return performanceService.getSnapshots();
  }

  @GetMapping("/realtime-stats")
  public List<Map<String, Object>> getRealtimeStats() {
    return performanceService.getDetailedRealtimeStats();
  }

  @GetMapping("/awr-metrics")
  public Map<String, Object> getAwrMetrics(
    @RequestParam String beginSnapId,
    @RequestParam String endSnapId
  ) {
    return performanceService.getAwrMetrics(beginSnapId, endSnapId);
  }
}
