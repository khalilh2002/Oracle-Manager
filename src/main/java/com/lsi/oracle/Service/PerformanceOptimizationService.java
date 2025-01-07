package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.SlowQueryDTO;
import com.lsi.oracle.config.exceptions.DatabaseOperationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
@Setter
public class PerformanceOptimizationService {

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Value("${oracle.monitoring.slow-query.threshold:1000000}")
  private long slowQueryThreshold;

  @Autowired
  private TaskScheduler taskScheduler;

  private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  public void setSlowQueryThreshold(long threshold) {
    if (threshold <= 0) {
      throw new IllegalArgumentException("Threshold must be greater than 0");
    }
    this.slowQueryThreshold = threshold;
    log.info("Slow query threshold updated to: {}", threshold);
  }

  public List<SlowQueryDTO> fetchSlowQueries() {
    String sql = """
            SELECT
                SQL_ID,
                SQL_TEXT,
                ELAPSED_TIME/1000000 as ELAPSED_SECONDS,
                EXECUTIONS,
                ROUND(ELAPSED_TIME/GREATEST(EXECUTIONS,1)/1000000, 2) as AVG_SECONDS_PER_EXEC,
                PARSING_SCHEMA_NAME,
                FIRST_LOAD_TIME,
                LAST_LOAD_TIME,
                BUFFER_GETS,
                DISK_READS,
                CPU_TIME/1000000 as CPU_SECONDS,
                ROWS_PROCESSED
            FROM V$SQL
            WHERE ELAPSED_TIME/GREATEST(EXECUTIONS,1) > :threshold
                AND PARSING_SCHEMA_NAME = USER
                AND SQL_TEXT NOT LIKE '%V$SQL%'
                AND SQL_TEXT NOT LIKE '%DBA_%'
            ORDER BY ELAPSED_TIME DESC
            FETCH FIRST 50 ROWS ONLY
            """;

    try {
      List<Map<String, Object>> results = jdbcTemplate.queryForList(sql,
        new MapSqlParameterSource("threshold", slowQueryThreshold));
      return results.stream()
        .map(SlowQueryDTO::fromMap)
        .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error fetching slow queries", e);
      throw new DatabaseOperationException("Failed to fetch slow queries", e);
    }
  }

  public Map<String, Object> tuneQuery(String sqlId) {
    validateSqlId(sqlId); // Validate SQL ID format

    // Check if SQL ID exists in the database
    Map<String, Object> sqlInfo = getSqlInfo(sqlId);
    if (sqlInfo == null) {
      throw new IllegalArgumentException("No SQL found with the provided SQL ID: " + sqlId);
    }

    String taskName = "SQL_TUNING_TASK_" + sqlId;

    try {
      // Attempt to create and execute a new tuning task
      createAndExecuteTuningTask(taskName, sqlInfo);

      // Monitor the tuning task for completion
      boolean isCompleted = monitorTuningTask(taskName);
      if (!isCompleted) {
        throw new RuntimeException("Tuning task did not complete in the expected time frame.");
      }

      // Fetch and return tuning recommendations and results
      return getTuningResults(taskName, sqlId, sqlInfo);
    } catch (Exception e) {
      log.error("Error while tuning query with SQL ID: {}", sqlId, e);
      throw new DatabaseOperationException("Failed to tune query: " + e.getMessage(), e);
    } finally {
      // Cleanup the tuning task to avoid lingering artifacts
      cleanupTuningTask(taskName);
    }
  }



  private Map<String, Object> getSqlInfo(String sqlId) {
    String sql = """
        SELECT SQL_ID, PARSING_SCHEMA_NAME, SQL_FULLTEXT,
               ELAPSED_TIME/1000000 AS ELAPSED_SECONDS,
               EXECUTIONS, CPU_TIME/1000000 AS CPU_SECONDS,
               BUFFER_GETS, DISK_READS
        FROM V$SQL
        WHERE SQL_ID = :sqlId
    """;

    try {
      return jdbcTemplate.queryForMap(sql, new MapSqlParameterSource("sqlId", sqlId));
    } catch (Exception e) {
      log.error("SQL ID {} not found", sqlId, e);
      return null;
    }
  }


  private void createAndExecuteTuningTask(String taskName, Map<String, Object> sqlInfo) {
    String createTaskSql = """
        DECLARE
            v_task_name VARCHAR2(100);
        BEGIN
            v_task_name := DBMS_SQLTUNE.CREATE_TUNING_TASK(
                sql_text    => :sqlText,
                user_name   => :schema,
                scope       => DBMS_SQLTUNE.SCOPE_COMPREHENSIVE,
                time_limit  => 60,
                task_name   => :taskName,
                description => 'Tuning task for SQL_ID: ' || :sqlId
            );
        END;
    """;

    executeDbmsTask(createTaskSql,
      new MapSqlParameterSource()
        .addValue("sqlText", sqlInfo.get("SQL_FULLTEXT"))
        .addValue("schema", sqlInfo.get("PARSING_SCHEMA_NAME"))
        .addValue("taskName", taskName)
        .addValue("sqlId", sqlInfo.get("SQL_ID")));

    String executeTaskSql = """
        BEGIN
            DBMS_SQLTUNE.EXECUTE_TUNING_TASK(task_name => :taskName);
        END;
    """;

    executeDbmsTask(executeTaskSql, new MapSqlParameterSource("taskName", taskName));
  }

  private boolean monitorTuningTask(String taskName) throws InterruptedException {
    String statusSql = """
        SELECT STATUS FROM DBA_ADVISOR_LOG
        WHERE TASK_NAME = :taskName
    """;

    for (int i = 0; i < 10; i++) {
      String status = jdbcTemplate.queryForObject(
        statusSql,
        new MapSqlParameterSource("taskName", taskName),
        String.class
      );

      if ("COMPLETED".equals(status)) {
        return true;
      }
      Thread.sleep(2000); // Poll every 2 seconds
    }
    return false;
  }

  private Map<String, Object> getTuningResults(String taskName, String sqlId, Map<String, Object> sqlInfo) {
    String reportSql = """
        SELECT DBMS_SQLTUNE.REPORT_TUNING_TASK(
            task_name => :taskName,
            type => 'TEXT'
        ) AS report FROM DUAL
    """;

    String report = jdbcTemplate.queryForObject(reportSql,
      new MapSqlParameterSource("taskName", taskName), String.class);

    Map<String, Object> result = new HashMap<>();
    result.put("sqlId", sqlId);
    result.put("taskName", taskName);
    result.put("recommendations", report);
    result.put("sqlInfo", sqlInfo);
    result.put("timestamp", LocalDateTime.now().toString());

    return result;
  }

  private void cleanupTuningTask(String taskName) {
    try {
      String sql = """
                BEGIN
                    DBMS_SQLTUNE.DROP_TUNING_TASK(task_name => :taskName);
                END;
                """;
      executeDbmsTask(sql, new MapSqlParameterSource("taskName", taskName));
    } catch (Exception e) {
      log.warn("Failed to cleanup tuning task: {}", taskName, e);
    }
  }




  public void scheduleStatsRecalculation(String frequency) {
    validateFrequency(frequency);

    String jobName = "RECALCULATE_STATS_" + frequency.toUpperCase();

    try {
      // Check if the job already exists
      String checkJobSql = """
            SELECT COUNT(*)
            FROM USER_SCHEDULER_JOBS
            WHERE JOB_NAME = :jobName
        """;

      int jobCount = jdbcTemplate.queryForObject(
        checkJobSql,
        new MapSqlParameterSource("jobName", jobName),
        Integer.class
      );

      // If the job exists, drop it
      if (jobCount > 0) {
        log.info("Dropping existing job: {}", jobName);
        String dropJobSql = "BEGIN DBMS_SCHEDULER.DROP_JOB(job_name => :jobName); END;";
        jdbcTemplate.update(dropJobSql, new MapSqlParameterSource("jobName", jobName));
      }

      // Create the new job
      String createJobSql = """
            BEGIN
                DBMS_SCHEDULER.CREATE_JOB(
                    job_name        => :jobName,
                    job_type        => 'PLSQL_BLOCK',
                    job_action      => '
                        BEGIN
                            DBMS_STATS.GATHER_SCHEMA_STATS(
                                ownname          => USER,
                                estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
                                method_opt       => ''FOR ALL COLUMNS SIZE AUTO'',
                                cascade          => TRUE
                            );
                        END;',
                    start_date      => SYSTIMESTAMP,
                    repeat_interval => :cronExpression,
                    enabled         => TRUE
                );
            END;
        """;

      String cronExpression = getCronExpressionForOracle(frequency);

      jdbcTemplate.update(
        createJobSql,
        new MapSqlParameterSource()
          .addValue("jobName", jobName)
          .addValue("cronExpression", cronExpression)
      );

      log.info("Scheduled Oracle statistics recalculation job '{}' with frequency: {}", jobName, frequency);

    } catch (Exception e) {
      log.error("Failed to schedule statistics recalculation for frequency: {}", frequency, e);
      throw new DatabaseOperationException("Failed to schedule statistics recalculation", e);
    }
  }
  private String getCronExpressionForOracle(String frequency) {
    return switch (frequency.toLowerCase()) {
      case "daily" -> "FREQ=DAILY;BYHOUR=2";       // Run daily at 2 AM
      case "weekly" -> "FREQ=WEEKLY;BYDAY=SUN;BYHOUR=2";  // Run weekly on Sunday at 2 AM
      case "monthly" -> "FREQ=MONTHLY;BYMONTHDAY=1;BYHOUR=2"; // Run monthly on the 1st at 2 AM
      default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
    };
  }


  public void scheduleStatsRecalculationSpringBoot(String frequency) {
    validateFrequency(frequency);
    String taskId = "STATS_" + frequency;

    synchronized (scheduledTasks) {
      ScheduledFuture<?> existing = scheduledTasks.get(taskId);
      if (existing != null) {
        existing.cancel(false);
      }

      CronTrigger trigger = new CronTrigger(getCronExpression(frequency));
      ScheduledFuture<?> future = taskScheduler.schedule(
        this::recalculateStats,
        trigger
      );

      scheduledTasks.put(taskId, future);
      log.info("Scheduled statistics recalculation for frequency: {}", frequency);
    }
  }

  private void recalculateStats() {
    try {
      String sql = """
                BEGIN
                    DBMS_STATS.GATHER_SCHEMA_STATS(
                        ownname => USER,
                        estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
                        method_opt => 'FOR ALL COLUMNS SIZE AUTO',
                        cascade => TRUE,
                        degree => 4,
                        no_invalidate => FALSE
                    );
                END;
                """;
      jdbcTemplate.update(sql, new MapSqlParameterSource());
      log.info("Statistics recalculated successfully");
    } catch (Exception e) {
      log.error("Error recalculating statistics", e);
      throw new DatabaseOperationException("Failed to recalculate statistics", e);
    }
  }

  private String getCronExpression(String frequency) {
    return switch (frequency.toLowerCase()) {
      case "daily" -> "0 0 2 * * ?";  // 2 AM every day
      case "weekly" -> "0 0 2 ? * SUN";  // 2 AM every Sunday
      case "monthly" -> "0 0 2 1 * ?";  // 2 AM on 1st of every month
      default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
    };
  }

  private void validateSqlId(String sqlId) {
    if (sqlId == null || !sqlId.matches("^[a-zA-Z0-9]{13}$")) {
      throw new IllegalArgumentException("Invalid SQL ID format");
    }
  }

  private void validateFrequency(String frequency) {
    if (frequency == null || !Arrays.asList("daily", "weekly", "monthly")
      .contains(frequency.toLowerCase())) {
      throw new IllegalArgumentException("Invalid frequency. Must be daily, weekly, or monthly");
    }
  }

  private void executeDbmsTask(String sql, MapSqlParameterSource params) {
    jdbcTemplate.update(sql, params);
  }




  public List<Map<String, Object>> getAllJobs() {
    String query = """
        SELECT JOB_NAME, PROGRAM_NAME, SCHEDULE_NAME, STATE, LAST_START_DATE, NEXT_RUN_DATE, REPEAT_INTERVAL
        FROM USER_SCHEDULER_JOBS
        ORDER BY JOB_NAME
    """;

    try {
      List<Map<String, Object>> jobs = jdbcTemplate.queryForList(query , new MapSqlParameterSource());
      log.info("Fetched {} jobs from Oracle Scheduler", jobs.size());
      return jobs;
    } catch (Exception e) {
      log.error("Error fetching jobs from Oracle Scheduler", e);
      throw new DatabaseOperationException("Failed to fetch jobs from Oracle Scheduler", e);
    }
  }

}
