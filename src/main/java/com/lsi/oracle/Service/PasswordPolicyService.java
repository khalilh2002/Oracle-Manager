package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.PasswordPolicyRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class PasswordPolicyService {
    private final JdbcTemplate jdbcTemplate;

    public PasswordPolicyService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createPolicy(PasswordPolicyRequest policyRequest) throws SQLException {
        String sql = String.format(
                "CREATE PROFILE %s LIMIT PASSWORD_LIFE_TIME %d PASSWORD_REUSE_TIME %d PASSWORD_LOCK_TIME %d",
                policyRequest.profileName(),
                policyRequest.passwordLifeTime(),
                policyRequest.passwordReuseTime(),
                policyRequest.passwordLockTime()
        );
        jdbcTemplate.execute(sql);
    }

    public void assignPolicyToUser(String username, String profileName) throws SQLException {
        String sql = String.format("ALTER USER %s PROFILE %s", username, profileName);
        jdbcTemplate.execute(sql);
    }

    public void deletePolicy(String profileName) throws SQLException {
        String sql = String.format("DROP PROFILE %s CASCADE", profileName);
        jdbcTemplate.execute(sql);
    }
}
