package com.zencube.registry.opening.repository;

import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.enums.OpeningStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpeningRepository extends JpaRepository<Opening, UUID>, JpaSpecificationExecutor<Opening> {

    List<Opening> findByEnterpriseIdAndDeletedFalse(UUID enterpriseId);
    Page<Opening> findByEnterpriseIdAndDeletedFalse(UUID enterpriseId, Pageable pageable);

    boolean existsByEnterpriseIdAndTitleIgnoreCaseAndDeletedFalse(UUID enterpriseId, String title);

    List<Opening> findByStatusAndDeletedFalse(OpeningStatus status);
    Page<Opening> findByStatusAndDeletedFalse(OpeningStatus status, Pageable pageable);

    long countByStatusAndDeletedFalse(OpeningStatus status);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM Opening o WHERE o.status = 'LIVE' AND o.deleted = false")
    long countLiveOpenings();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM Opening o WHERE o.status = 'CLOSED' AND o.deleted = false")
    long countClosedOpenings();

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Opening o WHERE o.status = 'CLOSED' AND o.deleted = false")
    List<Opening> findClosedOpenings();

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Opening o WHERE o.status = 'REVISION_REQUESTED' AND o.deleted = false")
    List<Opening> findRevisionRequested();

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Opening o WHERE o.status = 'LIVE' AND o.deleted = false AND (o.applicationDeadline IS NULL OR o.applicationDeadline > :now)")
    Page<Opening> findActiveLiveOpenings(@org.springframework.data.repository.query.Param("now") java.time.Instant now, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM Opening o WHERE o.status = 'REVISION_REQUESTED' AND o.deleted = false")
    long countRevisionRequested();

    Optional<Opening> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndEnterpriseIdAndDeletedFalse(UUID id, UUID enterpriseId);

    List<Opening> findByStatusAndPublishedAtIsNotNullAndDeletedFalse(OpeningStatus status);
}
