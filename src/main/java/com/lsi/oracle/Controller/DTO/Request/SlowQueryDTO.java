package com.lsi.oracle.Controller.DTO.Request;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SlowQueryDTO {
  private String sqlId;
  private String sqlText;
  private Double elapsedSeconds;
  private Long executions;
  private Double avgSecondsPerExec;
  private String parsingSchemaName;
  private String firstLoadTime;
  private String lastLoadTime;

  public static SlowQueryDTO fromMap(Map<String, Object> map) {
    return SlowQueryDTO.builder()
      .sqlId((String) map.get("SQL_ID"))
      .sqlText((String) map.get("SQL_TEXT"))
      .elapsedSeconds(((Number) map.get("ELAPSED_SECONDS")).doubleValue())
      .executions(((Number) map.get("EXECUTIONS")).longValue())
      .avgSecondsPerExec(((Number) map.get("AVG_SECONDS_PER_EXEC")).doubleValue())
      .parsingSchemaName((String) map.get("PARSING_SCHEMA_NAME"))
      .firstLoadTime((String) map.get("FIRST_LOAD_TIME"))
      .lastLoadTime((String) map.get("LAST_LOAD_TIME"))
      .build();
  }
}
