package com.lsi.oracle.Service;

import com.lsi.oracle.config.exceptions.DatabaseOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PerformanceService {
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Value("${oracle.monitoring.slow-query.threshold:1000000}")
  private long slowQueryThreshold;

  public String getDbId() {
    String sql = "SELECT DBID FROM V$DATABASE";
    return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), String.class);
  }

  public List<Map<String, Object>> getSnapshots() {
    String sql = """
            SELECT
                SNAP_ID,
                TO_CHAR(BEGIN_INTERVAL_TIME, 'YYYY-MM-DD HH24:MI:SS') as BEGIN_INTERVAL_TIME,
                TO_CHAR(END_INTERVAL_TIME, 'YYYY-MM-DD HH24:MI:SS') as END_INTERVAL_TIME
            FROM DBA_HIST_SNAPSHOT
            ORDER BY SNAP_ID DESC
            FETCH FIRST 100 ROWS ONLY
            """;
    return jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
  }

  public List<Map<String, Object>> getDetailedRealtimeStats() {
    String sql = """
            SELECT
                TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') as TIMESTAMP,
                'CPU Usage' as METRIC_NAME,
                (SELECT VALUE FROM V$SYSSTAT WHERE NAME = 'CPU used by this session') as VALUE,
                'Seconds' as METRIC_UNIT
            FROM DUAL
            UNION ALL
            SELECT
                TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'),
                'Memory Usage',
                (SELECT ROUND((TOTAL_MEMORY - FREE_MEMORY) / TOTAL_MEMORY * 100, 2)
                 FROM (SELECT * FROM V$MEMORY_DYNAMIC_COMPONENTS
                       WHERE COMPONENT = 'SGA Target')) as VALUE,
                'Percentage'
            FROM DUAL
            UNION ALL
            SELECT
                TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'),
                'I/O Usage',
                (SELECT VALUE FROM V$SYSSTAT
                 WHERE NAME = 'physical read total bytes') as VALUE,
                'Bytes'
            FROM DUAL
            """;

    try {
      return jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
    } catch (Exception e) {
      log.error("Error fetching real-time metrics", e);
      throw new DatabaseOperationException("Failed to fetch metrics", e);
    }
  }

  public Map<String, Object> getAwrMetrics(String beginSnapId, String endSnapId) {
    String sql = """
            SELECT
                METRIC_NAME,
                ROUND(AVG(AVERAGE), 2) as AVG_VALUE,
                MAX(MAXVAL) as MAX_VALUE,
                MIN(MINVAL) as MIN_VALUE,
                TO_CHAR(END_TIME, 'YYYY-MM-DD HH24:MI:SS') as TIME_SERIES
            FROM DBA_HIST_SYSMETRIC_SUMMARY
            WHERE SNAP_ID BETWEEN :beginSnapId AND :endSnapId
            AND METRIC_NAME IN (
                'CPU Usage Per Sec',
                'Database CPU Time Ratio',
                'Memory Sorts Ratio',
                'Physical Read Total Bytes Per Sec'
            )
            GROUP BY METRIC_NAME, END_TIME
            ORDER BY END_TIME
            """;

    try {
      MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("beginSnapId", beginSnapId)
        .addValue("endSnapId", endSnapId);

      List<Map<String, Object>> metrics = jdbcTemplate.queryForList(sql, params);
      return Map.of(
        "metrics", metrics,
        "summary", getSummaryStats(metrics)
      );
    } catch (Exception e) {
      log.error("Error fetching AWR metrics", e);
      throw new DatabaseOperationException("Failed to fetch AWR metrics", e);
    }
  }

  private Map<String, Object> getSummaryStats(List<Map<String, Object>> metrics) {
    Map<String, List<Double>> metricsByName = metrics.stream()
      .collect(Collectors.groupingBy(
        m -> (String) m.get("METRIC_NAME"),
        Collectors.mapping(
          m -> ((Number) m.get("AVG_VALUE")).doubleValue(),
          Collectors.toList()
        )
      ));

    Map<String, Object> summary = new HashMap<>();
    metricsByName.forEach((name, values) -> {
      DoubleSummaryStatistics stats = values.stream()
        .mapToDouble(Double::doubleValue)
        .summaryStatistics();

      summary.put(name, Map.of(
        "average", stats.getAverage(),
        "max", stats.getMax(),
        "min", stats.getMin(),
        "count", stats.getCount()
      ));
    });

    return summary;
  }
}
