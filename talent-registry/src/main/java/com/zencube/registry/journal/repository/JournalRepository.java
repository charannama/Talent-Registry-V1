package com.zencube.registry.journal.repository;

import com.zencube.registry.journal.entity.Journal;
import com.zencube.registry.journal.entity.JournalAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {

    List<Journal> findByJournableType(String journableType);

    List<Journal> findByJournableId(Long journableId);

    List<Journal> findByUserId(UUID userId);

    List<Journal> findByAction(JournalAction action);

    List<Journal> findByCreatedAtBetween(Instant startDate, Instant endDate);

    List<Journal> findByJournableTypeAndJournableId(String journableType, Long journableId);

    Page<Journal> findByJournableTypeAndJournableId(String journableType, Long journableId, Pageable pageable);

    Page<Journal> findByUserId(UUID userId, Pageable pageable);
}
