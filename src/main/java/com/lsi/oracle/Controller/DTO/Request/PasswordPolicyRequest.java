package com.lsi.oracle.Controller.DTO.Request;

public record PasswordPolicyRequest(
        String profileName,        // Profile name
        Integer passwordLifeTime,  // Days before password expires
        Integer passwordReuseTime, // Days before reuse allowed
        Integer passwordLockTime   // Days account is locked after failed attempts
) {}
