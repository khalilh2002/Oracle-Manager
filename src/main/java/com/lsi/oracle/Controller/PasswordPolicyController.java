package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.PasswordPolicyRequest;
import com.lsi.oracle.Service.PasswordPolicyService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class PasswordPolicyController {
    private final PasswordPolicyService passwordPolicyService;

    PasswordPolicyController(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    @PostMapping("/password-policy/create")
    public String createPasswordPolicy(@RequestBody PasswordPolicyRequest policyRequest) {
        try {
            passwordPolicyService.createPolicy(policyRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Password policy created successfully.";
    }

    @PostMapping("/password-policy/assign")
    public String assignPolicyToUser(@RequestParam String username, @RequestParam String profileName) {
        try {
            passwordPolicyService.assignPolicyToUser(username, profileName);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Password policy assigned successfully.";
    }

    @DeleteMapping("/password-policy/delete")
    public String deletePasswordPolicy(@RequestParam String profileName) {
        try {
            passwordPolicyService.deletePolicy(profileName);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Password policy deleted successfully.";
    }
}
