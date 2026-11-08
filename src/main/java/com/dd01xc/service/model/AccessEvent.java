package com.dd01xc.service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
//index for lockout lookups by account and time
@Table(name = "access_events", indexes = @Index(name = "idx_access_events_account_time", columnList = "usernameOrEmail, createdAt"))
public class AccessEvent {

    //const
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_LOCKED = "LOCKED";
    //separate from FAILED so duplicate signups cannot trigger login lockout
    public static final String STATUS_REGISTER_FAILED = "REGISTER_FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String usernameOrEmail;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "duration_ms")
    private Long durationMs;

    public Long getId() {
        return id;
    }
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setIpAddress(String ipAddress) {this.ipAddress = ipAddress;}
    public String getIpAddress() {
        return ipAddress;
    }
    public Long getDurationMs() {
        return durationMs;
    }
    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}