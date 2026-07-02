package com.zencube.registry.journal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "journals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journable_type", nullable = false, updatable = false)
    private String journableType;

    // Mapped as Long to match the BIGINT database column
    @Column(name = "journable_id", nullable = false, updatable = false)
    private Long journableId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false)
    private JournalAction action;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JournalDetail> details = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public void addDetail(JournalDetail detail) {
        details.add(detail);
        detail.setJournal(this);
    }

    public void removeDetail(JournalDetail detail) {
        details.remove(detail);
        detail.setJournal(null);
    }
}
