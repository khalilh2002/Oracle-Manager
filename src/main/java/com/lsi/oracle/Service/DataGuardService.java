package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.DataGuardRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataGuardService {
    private final JdbcTemplate jdbcTemplate;

    public DataGuardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String configureDataGuard(DataGuardRequest request) {
        String configureSql = String.format(
                "BEGIN " +
                        "DBMS_DG.INITIATE_CONFIG('%s', '%s'); " +
                        "END;",
                request.primaryDatabase(),
                request.standbyDatabase()
        );
        jdbcTemplate.execute(configureSql);
        return "Data Guard configuration initiated between " + request.primaryDatabase() + " and " + request.standbyDatabase();
    }

    public String monitorDataGuard(String primaryDatabase, String standbyDatabase) {
        String monitorSql = String.format(
                "SELECT STATUS FROM V$DATAGUARD_STATUS WHERE DATABASE_NAME IN ('%s', '%s')",
                primaryDatabase.toUpperCase(),
                standbyDatabase.toUpperCase()
        );
        return jdbcTemplate.queryForObject(monitorSql, String.class);
    }

    public String simulateFailover(DataGuardRequest request) {
        String failoverSql = String.format(
                "BEGIN " +
                        "DBMS_DG.FAILOVER('%s', '%s'); " +
                        "END;",
                request.primaryDatabase(),
                request.standbyDatabase()
        );
        jdbcTemplate.execute(failoverSql);
        return "Failover simulated from " + request.primaryDatabase() + " to " + request.standbyDatabase();
    }

    public String reinstatePrimary(DataGuardRequest request) {
        String reinstateSql = String.format(
                "BEGIN " +
                        "DBMS_DG.REINSTATE_PRIMARY('%s'); " +
                        "END;",
                request.primaryDatabase()
        );
        jdbcTemplate.execute(reinstateSql);
        return "Primary database " + request.primaryDatabase() + " reinstated successfully.";
    }
}
