package com.dd01xc.service.model;

public record ProfileResponseDTO (
    String username,
    String email,
    String role,
    String status,
    String createdAt,
    String lastLogin,
    String lastIp
) {}
