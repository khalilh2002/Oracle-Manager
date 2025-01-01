package com.lsi.oracle.Service;

import com.lsi.oracle.config.exceptions.DatabaseOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PerformanceService {
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Value("${oracle.monitoring.slow-query.threshold:1000000}")
  private long slowQueryThreshold;

  private final Queue<Map<String, Object>> realtimeMetricsCache = new LinkedList<>();
  private static final int CACHE_SIZE = 60; // Keep last 60 measurements

  @Scheduled(fixedRate = 5000) // Run every 5 seconds
  public void updateRealtimeMetrics() {
    try {
      Map<String, Object> metrics = getDetailedRealtimeStats();
      if (realtimeMetricsCache.size() >= CACHE_SIZE) {
        realtimeMetricsCache.poll();
      }
      realtimeMetricsCache.offer(metrics);
    } catch (Exception e) {
      log.error("Error updating realtime metrics", e);
    }
  }

  public List<Map<String, Object>> getRealtimeMetrics() {
    return new ArrayList<>(realtimeMetricsCache);
  }

  public Map<String, Object> getDetailedRealtimeStats() {
    String sql = """
            SELECT
                TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') as timestamp,
                (SELECT COUNT(*)
                 FROM V$SESSION
                 WHERE TYPE != 'BACKGROUND') as active_sessions,
                (SELECT ROUND(VALUE, 2)
                 FROM V$SYSSTAT
                 WHERE NAME = 'CPU used by this session') as cpu_usage,
                (SELECT ROUND(SUM(VALUE)/1024/1024, 2)
                 FROM V$SYSSTAT
                 WHERE NAME LIKE '%memory%') as memory_usage,
                (SELECT ROUND(SUM(BYTES)/1024/1024, 2)
                 FROM V$DATAFILE) as total_db_size_mb
            FROM DUAL
            """;
    try {
      Map<String, Object> stats = jdbcTemplate.queryForMap(sql, new MapSqlParameterSource());

      // Process the raw values
      Map<String, Object> processedStats = new HashMap<>();
      processedStats.put("timestamp", stats.get("TIMESTAMP"));
      processedStats.put("sessions", stats.get("ACTIVE_SESSIONS"));

      // CPU usage
      Double cpuUsage = ((Number) stats.get("CPU_USAGE")).doubleValue();
      processedStats.put("cpu", cpuUsage);

      // Memory in MB
      processedStats.put("memory", stats.get("MEMORY_USAGE"));

      // Database size in MB
      processedStats.put("database_size", stats.get("TOTAL_DB_SIZE_MB"));

      return processedStats;
    } catch (Exception e) {
      log.error("Error fetching realtime stats", e);
      throw new DatabaseOperationException("Failed to fetch realtime statistics", e);
    }
  }

  public Map<String, Object> getDetailedSystemMetrics() {
    String sql = """
            SELECT
                -- System Time
                TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') as current_time,

                -- Session Metrics
                (SELECT COUNT(*)
                 FROM V$SESSION
                 WHERE TYPE != 'BACKGROUND') as user_sessions,

                -- Memory Metrics
                (SELECT ROUND(SUM(VALUE)/1024/1024, 2)
                 FROM V$SYSSTAT
                 WHERE NAME LIKE '%memory%') as total_memory_used_mb,

                -- Database Size
                (SELECT ROUND(SUM(BYTES)/1024/1024, 2)
                 FROM V$DATAFILE) as total_database_size_mb,

                -- System Statistics
                (SELECT ROUND(VALUE, 2)
                 FROM V$SYSSTAT
                 WHERE NAME = 'physical reads') as physical_reads,

                (SELECT ROUND(VALUE, 2)
                 FROM V$SYSSTAT
                 WHERE NAME = 'physical writes') as physical_writes,

                -- Session CPU Usage
                (SELECT ROUND(VALUE, 2)
                 FROM V$SYSSTAT
                 WHERE NAME = 'CPU used by this session') as cpu_usage_value
            FROM DUAL
            """;

    try {
      return jdbcTemplate.queryForMap(sql, new MapSqlParameterSource());
    } catch (Exception e) {
      log.error("Error fetching detailed system metrics", e);
      throw new RuntimeException("Failed to fetch system metrics", e);
    }
  }

  public Map<String, Object> getIOStats() {
    String sql = """
            SELECT
                d.FILE# as file_id,
                d.NAME as datafile_name,
                ROUND(d.BYTES/1024/1024, 2) as size_mb,
                (SELECT COUNT(*)
                 FROM V$SESSION s
                 WHERE s.ROW_WAIT_FILE# = d.FILE#) as active_io_sessions
            FROM V$DATAFILE d
            ORDER BY d.FILE#
            """;

    try {
      return Map.of("datafiles", jdbcTemplate.queryForList(sql, new MapSqlParameterSource()));
    } catch (Exception e) {
      log.error("Error fetching I/O statistics", e);
      throw new RuntimeException("Failed to fetch I/O statistics", e);
    }
  }
}
