package com.dd01xc.service.model;

public record LoginResponse(
    String token,
    String email,
    String role) {}
