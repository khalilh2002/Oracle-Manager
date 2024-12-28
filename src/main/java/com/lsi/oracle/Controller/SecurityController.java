package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.EncryptionPolicyRequest;
import com.lsi.oracle.Controller.DTO.Request.AuditPolicyRequest;
import com.lsi.oracle.Controller.DTO.Request.VPDPolicyRequest;
import com.lsi.oracle.Service.SecurityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security")
public class SecurityController {
    private final SecurityService securityService;

    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    // Endpoint for configuring encryption policies
    @PostMapping("/encryption")
    public String configureEncryption(@RequestBody EncryptionPolicyRequest request) {
        try {
            securityService.configureEncryption(request);
            return "Encryption policy applied successfully.";
        } catch (Exception e) {
            return "Error configuring encryption: " + e.getMessage();
        }
    }

    // Endpoint for enabling/disabling audit policies
    @PostMapping("/audit")
    public String configureAudit(@RequestBody AuditPolicyRequest request) {
        try {
            securityService.configureAudit(request);
            return "Audit policy applied successfully.";
        } catch (Exception e) {
            return "Error configuring audit policy: " + e.getMessage();
        }
    }

    // Endpoint for managing VPD policies
    @PostMapping("/vpd")
    public String configureVPD(@RequestBody VPDPolicyRequest request) {
        try {
            securityService.configureVPD(request);
            return "VPD policy applied successfully.";
        } catch (Exception e) {
            return "Error configuring VPD: " + e.getMessage();
        }
    }
}
