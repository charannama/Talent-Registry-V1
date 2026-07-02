package com.zencube.registry.journal.service;

import com.zencube.registry.journal.dto.JournalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditQueryService {
    Page<JournalResponse> getEntityHistory(String entityType, Long entityId, Pageable pageable);
    Page<JournalResponse> getUserHistory(UUID userId, Pageable pageable);
    Page<JournalResponse> getRecentActivities(Pageable pageable);
}
