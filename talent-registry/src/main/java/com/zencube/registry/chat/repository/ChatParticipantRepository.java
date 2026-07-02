package com.zencube.registry.chat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import com.zencube.registry.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    List<ChatParticipant> findByThreadId(UUID threadId);

    boolean existsByThreadIdAndUserId(UUID threadId, UUID userId);

    Optional<ChatParticipant> findByThreadIdAndUserId(UUID threadId, UUID userId);
    
    List<ChatParticipant> findByThreadIdAndUserIdNot(UUID threadId, UUID userId);

    @Modifying
    @Query("UPDATE ChatParticipant p SET p.lastReadAt = :timestamp WHERE p.id = :participantId")
    void updateLastReadAt(@Param("participantId") UUID participantId, @Param("timestamp") java.time.Instant timestamp);
}
