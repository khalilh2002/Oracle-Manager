package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.QuotaRequest;
import com.lsi.oracle.Service.QuotaService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class QuotaController {
    private final QuotaService quotaService;

    QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @PostMapping("/quota/assign")
    public String assignQuota(@RequestBody QuotaRequest quotaRequest) {
        try {
            quotaService.assignQuota(quotaRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Quota assigned successfully.";
    }

    @PostMapping("/quota/remove")
    public String removeQuota(@RequestBody QuotaRequest quotaRequest) {
        try {
            quotaService.removeQuota(quotaRequest);
        } catch (SQLException e) {
            return "SQL EXCEPTION::" + e.getMessage();
        }
        return "Quota removed successfully.";
    }
}
