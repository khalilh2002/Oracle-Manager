package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.RoleRequest;
import com.lsi.oracle.Service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class RoleController {
    private final RoleService roleService;

    RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/role/create")
    public String createRole(@RequestBody RoleRequest roleRequest) {
        try {
            roleService.createRole(roleRequest.roleName());
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Role " + roleRequest.roleName() + " created successfully.";
    }

    @PostMapping("/role/grant")
    public String grantPrivilege(@RequestBody RoleRequest roleRequest) {
        try {
            roleService.grantPrivilege(roleRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Privilege granted successfully.";
    }

    @PostMapping("/role/revoke")
    public String revokePrivilege(@RequestBody RoleRequest roleRequest) {
        try {
            roleService.revokePrivilege(roleRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Privilege revoked successfully.";
    }

    @DeleteMapping("/role/delete")
    public String deleteRole(@RequestBody RoleRequest roleRequest) {
        try {
            roleService.deleteRole(roleRequest.roleName());
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "Role " + roleRequest.roleName() + " deleted successfully.";
    }
}
