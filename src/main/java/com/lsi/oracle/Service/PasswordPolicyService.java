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
        // Construct SQL query using concatenation and ensure proper formatting for special characters
        String sql = "ALTER USER " + username + " PROFILE " + profileName ;
        System.out.println("Executing SQL: " + sql); // Debugging log
        jdbcTemplate.execute(sql);
    }

    public void deletePolicy(String profileName) throws SQLException {
        // Ensure the profile name is properly quoted
        String sql = "DROP PROFILE " + profileName + " CASCADE";
        System.out.println("Executing SQL: " + sql); // Debugging log
        jdbcTemplate.execute(sql);
    }
}
