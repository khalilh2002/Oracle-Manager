package com.lsi.oracle.Controller.DTO.Request;

public record QuotaRequest(
        String username,        // Target user
        String tablespaceName,  // Target tablespace
        Long quotaSize          // Quota in MB, use -1 for unlimited
) {}
