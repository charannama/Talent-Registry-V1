package com.zencube.registry.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "taggings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tag_id", "taggable_type", "taggable_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tagging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "taggable_type", nullable = false, updatable = false)
    private String taggableType;

    @Column(name = "taggable_id", nullable = false, updatable = false)
    private String taggableId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
