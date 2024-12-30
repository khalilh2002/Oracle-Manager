package com.lsi.oracle.Service;

import com.lsi.oracle.config.exceptions.DatabaseOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

// Enhanced PerformanceOptimizationService.java
@Service
@Slf4j
public class PerformanceOptimizationService {
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Value("${oracle.monitoring.slow-query.threshold:1000000}")
  private long slowQueryThreshold;

  @Autowired
  private TaskScheduler taskScheduler;

  private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  public List<Map<String, Object>> fetchSlowQueries() {
    String sql = """
            SELECT
                SQL_ID,
                SQL_TEXT,
                ELAPSED_TIME/1000000 as ELAPSED_SECONDS,
                EXECUTIONS,
                ROUND(ELAPSED_TIME/GREATEST(EXECUTIONS,1)/1000000, 2) as AVG_SECONDS_PER_EXEC,
                PARSING_SCHEMA_NAME,
                FIRST_LOAD_TIME,
                LAST_LOAD_TIME
            FROM V$SQL
            WHERE ELAPSED_TIME/GREATEST(EXECUTIONS,1) > :threshold
            ORDER BY ELAPSED_TIME DESC
            FETCH FIRST 50 ROWS ONLY
            """;

    try {
      return jdbcTemplate.queryForList(sql,
        new MapSqlParameterSource("threshold", slowQueryThreshold));
    } catch (Exception e) {
      log.error("Error fetching slow queries", e);
      throw new DatabaseOperationException("Failed to fetch slow queries", e);
    }
  }

  public Map<String, Object> tuneQuery(String sqlId) {
    String taskName = "TUNE_" + sqlId + "_" + System.currentTimeMillis();

    try {
      // Create and execute tuning task
      executeDbmsTask("""
                BEGIN
                    DBMS_SQLTUNE.CREATE_TUNING_TASK(
                        sql_id => :sqlId,
                        scope => 'COMPREHENSIVE',
                        task_name => :taskName,
                        time_limit => 500
                    );
                    DBMS_SQLTUNE.EXECUTE_TUNING_TASK(task_name => :taskName);
                END;
                """,
        new MapSqlParameterSource()
          .addValue("sqlId", sqlId)
          .addValue("taskName", taskName)
      );

      // Get recommendations
      String report = jdbcTemplate.queryForObject(
        "SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(:taskName) FROM DUAL",
        new MapSqlParameterSource("taskName", taskName),
        String.class
      );

      return Map.of(
        "sqlId", sqlId,
        "taskName", taskName,
        "recommendations", report
      );
    } catch (Exception e) {
      log.error("Error tuning query", e);
      throw new DatabaseOperationException("Failed to tune query: " + sqlId, e);
    }
  }

  public void scheduleStatsRecalculation(String frequency) {
    String taskId = "STATS_" + frequency;
    ScheduledFuture<?> existing = scheduledTasks.get(taskId);
    if (existing != null) {
      existing.cancel(false);
    }

    CronTrigger trigger = new CronTrigger(getCronExpression(frequency));
    ScheduledFuture<?> future = taskScheduler.schedule(
      () -> recalculateStats(),
      trigger
    );

    scheduledTasks.put(taskId, future);
  }

  private void recalculateStats() {
    try {
      String sql = """
                BEGIN
                    DBMS_STATS.GATHER_SCHEMA_STATS(
                        ownname => USER,
                        estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
                        method_opt => 'FOR ALL COLUMNS SIZE AUTO',
                        cascade => TRUE
                    );
                END;
                """;
      jdbcTemplate.update(sql, new MapSqlParameterSource());
      log.info("Statistics recalculated successfully.");
    } catch (Exception e) {
      log.error("Error recalculating statistics", e);
      throw new DatabaseOperationException("Failed to recalculate statistics", e);
    }
  }


  private String getCronExpression(String frequency) {
    return switch (frequency.toLowerCase()) {
      case "daily" -> "0 0 2 * * ?";
      case "weekly" -> "0 0 2 ? * MON";
      case "monthly" -> "0 0 2 1 * ?";
      default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
    };
  }

  private void executeDbmsTask(String sql, MapSqlParameterSource params) {
    jdbcTemplate.update(sql, params);
  }
}
