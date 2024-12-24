package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.QuotaRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class QuotaService {
    private final JdbcTemplate jdbcTemplate;

    public QuotaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void assignQuota(QuotaRequest quotaRequest) throws SQLException {
        String sql = quotaRequest.quotaSize() > 0
                ? String.format("ALTER USER %s QUOTA %dM ON %s", quotaRequest.username(), quotaRequest.quotaSize(), quotaRequest.tablespaceName())
                : String.format("ALTER USER %s QUOTA UNLIMITED ON %s", quotaRequest.username(), quotaRequest.tablespaceName());
        jdbcTemplate.execute(sql);
    }

    public void removeQuota(QuotaRequest quotaRequest) throws SQLException {
        String sql = String.format("ALTER USER %s QUOTA 0 ON %s", quotaRequest.username(), quotaRequest.tablespaceName());
        jdbcTemplate.execute(sql);
    }
}
