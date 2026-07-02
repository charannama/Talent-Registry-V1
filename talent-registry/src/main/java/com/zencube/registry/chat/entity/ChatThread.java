package com.zencube.registry.chat.entity;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.chat.enums.ThreadType;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "chat_threads", indexes = {
        @Index(name = "idx_chat_thread_type", columnList = "thread_type"),
        @Index(name = "idx_chat_thread_context", columnList = "contextable_type, contextable_id"),
        @Index(name = "idx_chat_thread_creator", columnList = "created_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatThread extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "thread_type", nullable = false, length = 50)
    private ThreadType threadType;

    @Column(name = "contextable_type", length = 100)
    private String contextableType;

    @Column(name = "contextable_id")
    private UUID contextableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;

}

