package com.zencube.registry.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_audits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_id", nullable = true)
    private UUID notificationId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "action", nullable = false, length = 100)
    private String action; // e.g. NOTIFICATION_CREATED, NOTIFICATION_READ, NOTIFICATIONS_MARKED_READ

    @Column(name = "details", length = 500)
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
