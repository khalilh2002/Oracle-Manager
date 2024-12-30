package com.lsi.oracle.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PerformanceMetrics {
  private LocalDateTime timestamp;
  private double cpuUsage;
  private double ioRate;
  private double memoryUsage;
  private double activeConnections;
}
