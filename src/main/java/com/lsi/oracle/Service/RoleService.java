package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.RoleRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class RoleService {
    private final JdbcTemplate jdbcTemplate;

    public RoleService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createRole(String roleName) throws SQLException {
        String sql = String.format("CREATE ROLE %s", roleName.toUpperCase());
        jdbcTemplate.execute(sql);
    }

    public void grantPrivilege(RoleRequest roleRequest) throws SQLException {
        String sql;
        if (roleRequest.grantToUser() != null) {
            sql = String.format(
                    "GRANT %s TO %s %s",
                    roleRequest.roleName(),
                    roleRequest.grantToUser(),
                    roleRequest.withAdminOption() ? "WITH ADMIN OPTION" : ""
            );
        } else {
            sql = String.format(
                    "GRANT %s TO %s",
                    roleRequest.privilege(),
                    roleRequest.roleName()
            );
        }
        jdbcTemplate.execute(sql);
    }

    public void revokePrivilege(RoleRequest roleRequest) throws SQLException {
        String sql;
        if (roleRequest.grantToUser() != null) {
            sql = String.format("REVOKE %s FROM %s", roleRequest.roleName(), roleRequest.grantToUser());
        } else {
            sql = String.format("REVOKE %s FROM %s", roleRequest.privilege(), roleRequest.roleName());
        }
        jdbcTemplate.execute(sql);
    }

    public void deleteRole(String roleName) throws SQLException {
        String sql = String.format("DROP ROLE %s", roleName.toUpperCase());
        jdbcTemplate.execute(sql);
    }
}
