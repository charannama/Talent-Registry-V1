package com.zencube.registry.chat.entity;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_participants", indexes = {
        @Index(name = "idx_chat_participant_thread", columnList = "thread_id"),
        @Index(name = "idx_chat_participant_user", columnList = "user_id"),
        @Index(name = "idx_chat_participant_last_read", columnList = "last_read_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_participant_thread_user", columnNames = {"thread_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private ChatThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

}
