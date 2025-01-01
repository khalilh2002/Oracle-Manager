package com.lsi.oracle.Service;

import com.lsi.oracle.Controller.DTO.Request.EncryptionPolicyRequest;
import com.lsi.oracle.Controller.DTO.Request.AuditPolicyRequest;
import com.lsi.oracle.Controller.DTO.Request.VPDPolicyRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    private final JdbcTemplate jdbcTemplate;

    public SecurityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Configure TDE Encryption
    public void configureEncryption(EncryptionPolicyRequest request) {
        // Adjusting to use the correct syntax for TDE column encryption
        String sql = String.format(
                "ALTER TABLE %s MODIFY (%s ENCRYPT USING '%s')",
                request.getTableName(),
                request.getColumnName(),
                request.getEncryptionAlgorithm()
        );
        jdbcTemplate.execute(sql);
    }


    // Configure Security Audit
    public void configureAudit(AuditPolicyRequest request) {
        // Using String.format() for audit policy SQL query
        String sql = String.format(
                "AUDIT %s ON %s",
                request.getActionName(),
                request.getObjectName()
        );
        jdbcTemplate.execute(sql);
    }

    // Configure VPD Policy
    // Configure VPD Policy with dynamic schema
    public void configureVPD(VPDPolicyRequest request) {
        String schemaName = request.getSchemaName() != null ? request.getSchemaName() : "C##HR";  // Default to C##HR if schemaName is not provided
        String statementTypes = request.getStatementTypes() != null ? request.getStatementTypes() : "SELECT"; // Default to SELECT if not provided

        String sql = String.format(
                "BEGIN " +
                        "DBMS_RLS.ADD_POLICY(" +
                        "object_schema => '%s', " +
                        "object_name => '%s', " +
                        "policy_name => '%s', " +
                        "function_schema => '%s', " +
                        "policy_function => '%s', " +
                        "statement_types => '%s' " +
                        "); " +
                        "END;",
                schemaName,
                request.getTableName(),
                request.getPolicyName(),
                schemaName,  // Function schema is the same as object schema
                request.getPolicyFunction(),
                statementTypes
        );
        jdbcTemplate.execute(sql);
    }
}
