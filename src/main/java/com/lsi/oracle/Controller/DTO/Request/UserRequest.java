package com.lsi.oracle.Controller.DTO.Request;

public record UserRequest(
        String username,
        String password,
        String role,
        String defaultTablespace,
        String tempTablespace
) {}
