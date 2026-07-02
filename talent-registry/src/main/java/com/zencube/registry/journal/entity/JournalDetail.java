package com.zencube.registry.journal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "journal_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false, updatable = false)
    private Journal journal;

    @Column(name = "field_name", nullable = false, updatable = false)
    private String fieldName;

    @Column(name = "old_value", updatable = false)
    private String oldValue;

    @Column(name = "new_value", updatable = false)
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
