package com.zencube.registry.chat.repository;

import com.zencube.registry.chat.entity.ChatThread;
import com.zencube.registry.chat.enums.ThreadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThread, UUID> {



    Page<ChatThread> findByCreatorId(UUID creatorId, Pageable pageable);
    
    @Query("SELECT t FROM ChatThread t WHERE t.threadType = :threadType AND t.isArchived = :isArchived")
    Page<ChatThread> findByThreadTypeAndIsArchived(@Param("threadType") ThreadType threadType, @Param("isArchived") boolean isArchived, Pageable pageable);
    
    Optional<ChatThread> findByContextableTypeAndContextableIdAndThreadType(String contextableType, UUID contextableId, ThreadType threadType);

    Optional<ChatThread> findByContextableTypeAndContextableId(String contextableType, UUID contextableId);

    boolean existsByContextableTypeAndContextableId(String contextableType, UUID contextableId);

    @Query("SELECT t FROM ChatThread t JOIN ChatParticipant p ON t.id = p.thread.id WHERE p.user.id = :userId ORDER BY t.updatedAt DESC")
    Page<ChatThread> findThreadsForUser(@Param("userId") UUID userId, Pageable pageable);

}
