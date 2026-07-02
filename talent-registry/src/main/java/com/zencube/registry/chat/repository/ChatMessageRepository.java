package com.zencube.registry.chat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zencube.registry.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findByThreadIdOrderBySentAtAsc(UUID threadId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.thread.id = :threadId AND m.sender.id != :userId AND m.deletedAt IS NULL")
    long countAllUnreadMessages(@Param("threadId") UUID threadId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.thread.id = :threadId AND m.sender.id != :userId AND m.sentAt > :lastReadAt AND m.deletedAt IS NULL")
    long countUnreadMessages(@Param("threadId") UUID threadId, @Param("userId") UUID userId, @Param("lastReadAt") java.time.Instant lastReadAt);

    @Query("SELECT p.thread.id, COUNT(m) FROM ChatParticipant p JOIN ChatMessage m ON p.thread.id = m.thread.id " +
           "WHERE p.user.id = :userId AND m.sender.id != :userId AND m.deletedAt IS NULL AND (p.lastReadAt IS NULL OR m.sentAt > p.lastReadAt) " +
           "AND p.thread.id IN :threadIds GROUP BY p.thread.id")
    java.util.List<Object[]> countUnreadMessagesBatch(@Param("userId") UUID userId, @Param("threadIds") java.util.Collection<UUID> threadIds);

}
