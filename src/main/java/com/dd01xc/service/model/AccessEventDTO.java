package com.dd01xc.service.model;

import java.time.LocalDateTime;

import com.dd01xc.service.util.Masking;

//public view of AccessEvent with masked account and IP
public record AccessEventDTO(
    Long id,
    String usernameOrEmail,
    String status,
    LocalDateTime createdAt,
    String ipAddress,
    Long durationMs
) {
    public static AccessEventDTO from(AccessEvent e) {
        return new AccessEventDTO(
            e.getId(),
            Masking.account(e.getUsernameOrEmail()),
            e.getStatus(),
            e.getCreatedAt(),
            Masking.ip(e.getIpAddress()),
            e.getDurationMs()
        );
    }
}
