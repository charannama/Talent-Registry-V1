package com.zencube.registry.journal.service;

import com.zencube.registry.journal.entity.Journal;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.journal.entity.JournalDetail;
import com.zencube.registry.journal.repository.JournalRepository;
import com.zencube.registry.journal.util.EntityComparator;
import com.zencube.registry.journal.util.FieldChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final JournalRepository journalRepository;
    private final EntityComparator entityComparator;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordCreate(String entityType, Long entityId) {
        Journal journal = createJournal(entityType, entityId, JournalAction.CREATE);
        journalRepository.save(journal);
        log.debug("Recorded CREATE audit for {} id={}", entityType, entityId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordUpdate(String entityType, Long entityId, Object beforeEntity, Object afterEntity) {
        List<FieldChange> changes = entityComparator.compare(beforeEntity, afterEntity);
        if (changes.isEmpty()) {
            log.debug("No meaningful changes detected for UPDATE audit on {} id={}", entityType, entityId);
            return;
        }

        Journal journal = createJournal(entityType, entityId, JournalAction.UPDATE);
        
        for (FieldChange change : changes) {
            JournalDetail detail = JournalDetail.builder()
                    .fieldName(change.getFieldName())
                    .oldValue(change.getOldValue())
                    .newValue(change.getNewValue())
                    .build();
            journal.addDetail(detail);
        }

        journalRepository.save(journal);
        log.debug("Recorded UPDATE audit with {} field changes for {} id={}", changes.size(), entityType, entityId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordDelete(String entityType, Long entityId) {
        Journal journal = createJournal(entityType, entityId, JournalAction.DELETE);
        journalRepository.save(journal);
        log.debug("Recorded DELETE audit for {} id={}", entityType, entityId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordCustomEvent(String eventType, String resourceType, String resourceId, String details) {
        Journal journal = Journal.builder()
                .journableType(resourceType)
                .journableId(0L) // Default or dummy ID for custom events where entityId might be a UUID
                .userId(getCurrentUserId())
                .action(JournalAction.CUSTOM)
                .build();
                
        journal.addDetail(JournalDetail.builder()
                .fieldName("eventType")
                .oldValue(null)
                .newValue(eventType)
                .build());
                
        journal.addDetail(JournalDetail.builder()
                .fieldName("resourceId")
                .oldValue(null)
                .newValue(resourceId)
                .build());
                
        journal.addDetail(JournalDetail.builder()
                .fieldName("details")
                .oldValue(null)
                .newValue(details)
                .build());

        journalRepository.save(journal);
        log.debug("Recorded CUSTOM audit event '{}' for {} id={}", eventType, resourceType, resourceId);
    }

    private Journal createJournal(String entityType, Long entityId, JournalAction action) {
        return Journal.builder()
                .journableType(entityType)
                .journableId(entityId)
                .userId(getCurrentUserId())
                .action(action)
                .build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Default System UUID when no context is present (e.g., background jobs)
            return UUID.fromString("00000000-0000-0000-0000-000000000000"); 
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            log.warn("Authenticated user ID is not a UUID: {}", auth.getName());
            return UUID.fromString("00000000-0000-0000-0000-000000000000"); 
        }
    }
}
