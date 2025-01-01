package com.lsi.oracle.Controller;

import com.lsi.oracle.Controller.DTO.Request.DataGuardRequest;
import com.lsi.oracle.Service.DataGuardService;
import org.springframework.web.bind.annotation.*;

@RestController
public class DataGuardController {
    private final DataGuardService dataGuardService;

    public DataGuardController(DataGuardService dataGuardService) {
        this.dataGuardService = dataGuardService;
    }

    @PostMapping("/dataguard/configure")
    public String configureDataGuard(@RequestBody DataGuardRequest request) {
        try {
            return dataGuardService.configureDataGuard(request);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/dataguard/monitor")
    public String monitorDataGuard(@RequestParam String primaryDatabase, @RequestParam String standbyDatabase) {
        try {
            return dataGuardService.monitorDataGuard(primaryDatabase, standbyDatabase);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @PostMapping("/dataguard/failover")
    public String simulateFailover(@RequestBody DataGuardRequest request) {
        try {
            return dataGuardService.simulateFailover(request);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @PostMapping("/dataguard/reinstate")
    public String reinstatePrimary(@RequestBody DataGuardRequest request) {
        try {
            return dataGuardService.reinstatePrimary(request);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
