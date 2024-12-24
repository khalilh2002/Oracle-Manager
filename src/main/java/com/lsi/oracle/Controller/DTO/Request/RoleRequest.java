package com.lsi.oracle.Controller.DTO.Request;

public record RoleRequest(
        String roleName,
        String privilege, // e.g., "SELECT ON SCHEMA.TABLE", "CREATE TABLE"
        String grantToUser, // User to whom this role/privilege is granted
        boolean withAdminOption // Whether to grant with admin option
) {}
