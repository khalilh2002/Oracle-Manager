package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.PasswordPolicyRequest;
import com.lsi.oracle.Controller.DTO.Request.AssignPolicyRequest;
import com.lsi.oracle.Controller.DTO.Request.DeletePolicyRequest;
import com.lsi.oracle.Service.PasswordPolicyService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class PasswordPolicyController {
    private final PasswordPolicyService passwordPolicyService;

    PasswordPolicyController(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    // Create password policy (using PasswordPolicyRequest)
    @PostMapping("/password-policy/create")
    public String createPasswordPolicy(@RequestBody PasswordPolicyRequest policyRequest) {
        try {
            passwordPolicyService.createPolicy(policyRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Password policy created successfully.";
    }

    // Assign password policy to a user (using AssignPolicyRequest)
    @PostMapping("/password-policy/assign")
    public String assignPolicyToUser(@RequestBody AssignPolicyRequest assignPolicyRequest) {
        try {
            passwordPolicyService.assignPolicyToUser(assignPolicyRequest.getUsername(), assignPolicyRequest.getProfileName());
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Password policy assigned successfully.";
    }

    // Delete password policy (using DeletePolicyRequest)
    @DeleteMapping("/password-policy/delete")
    public String deletePasswordPolicy(@RequestBody DeletePolicyRequest deletePolicyRequest) {
        try {
            passwordPolicyService.deletePolicy(deletePolicyRequest.getProfileName());
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Password policy deleted successfully.";
    }
}
