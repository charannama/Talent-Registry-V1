package com.zencube.registry.journal.service;

import com.zencube.registry.journal.dto.JournalDetailResponse;
import com.zencube.registry.journal.dto.JournalResponse;
import com.zencube.registry.journal.entity.Journal;
import com.zencube.registry.journal.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditQueryServiceImpl implements AuditQueryService {

    private final JournalRepository journalRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<JournalResponse> getEntityHistory(String entityType, Long entityId, Pageable pageable) {
        return journalRepository.findByJournableTypeAndJournableId(entityType, entityId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JournalResponse> getUserHistory(UUID userId, Pageable pageable) {
        return journalRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JournalResponse> getRecentActivities(Pageable pageable) {
        return journalRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private JournalResponse mapToResponse(Journal journal) {
        return JournalResponse.builder()
                .id(journal.getId())
                .journableType(journal.getJournableType())
                .journableId(journal.getJournableId())
                .action(journal.getAction())
                .actorId(journal.getUserId())
                .createdAt(journal.getCreatedAt())
                .changes(journal.getDetails().stream()
                        .map(detail -> JournalDetailResponse.builder()
                                .fieldName(detail.getFieldName())
                                .oldValue(detail.getOldValue())
                                .newValue(detail.getNewValue())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
