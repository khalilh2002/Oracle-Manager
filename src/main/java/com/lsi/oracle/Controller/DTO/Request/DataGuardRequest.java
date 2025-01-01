package com.lsi.oracle.Controller.DTO.Request;


public record DataGuardRequest(
        String primaryDatabase,
        String standbyDatabase,
        String action // "configure", "monitor", "failover", "reinstate"
) {}
