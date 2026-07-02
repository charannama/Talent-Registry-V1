package com.zencube.registry.journal.repository;

import com.zencube.registry.journal.entity.JournalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalDetailRepository extends JpaRepository<JournalDetail, Long> {

    List<JournalDetail> findByJournalId(Long journalId);

    List<JournalDetail> findByFieldName(String fieldName);

    List<JournalDetail> findByJournalIdAndFieldName(Long journalId, String fieldName);
}
