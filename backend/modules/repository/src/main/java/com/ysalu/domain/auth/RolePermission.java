package com.ysalu.domain.auth;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "role_permission")
// 角色权限。
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private SystemRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private SystemPermission permission;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    public Long getId() {
        return id;
    }

    public SystemRole getRole() {
        return role;
    }

    public void setRole(SystemRole role) {
        this.role = role;
    }

    public SystemPermission getPermission() {
        return permission;
    }

    public void setPermission(SystemPermission permission) {
        this.permission = permission;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    @PrePersist
    public void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}

