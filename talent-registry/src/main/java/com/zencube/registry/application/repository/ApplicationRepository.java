package com.zencube.registry.application.repository;

import com.zencube.registry.application.entity.Application;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.dashboard.dto.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {

    @Query("SELECT COUNT(a) FROM Application a WHERE a.profile.id = :profileId AND a.status IN :statuses")
    int countActiveApplications(@Param("profileId") UUID profileId, @Param("statuses") List<ApplicationStatus> statuses);

    @Query("SELECT a.status FROM Application a WHERE a.profile.id = :profileId AND a.status IN :statuses ORDER BY a.updatedAt DESC")
    List<ApplicationStatus> findMostAdvancedStatus(@Param("profileId") UUID profileId, @Param("statuses") List<ApplicationStatus> statuses);

    boolean existsByProfileIdAndOpeningIdAndDeletedFalse(UUID profileId, UUID openingId);

    Optional<Application> findByProfileIdAndOpeningId(UUID profileId, UUID openingId);

    boolean existsByProfileIdAndOpeningId(UUID profileId, UUID openingId);

    List<Application> findByProfileId(UUID profileId);

    @Query("SELECT a.status as status, COUNT(a) as count FROM Application a GROUP BY a.status")
    List<StatusCountProjection> countApplicationsByStatus();

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status IN :statuses")
    long countApplicationsByStatuses(@Param("statuses") List<ApplicationStatus> statuses);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status AND a.updatedAt < :threshold")
    long countStaleApplications(@Param("status") ApplicationStatus status, @Param("threshold") Instant threshold);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.createdAt >= :timestamp")
    long countApplicationsCreatedSince(@Param("timestamp") Instant timestamp);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    long countByStatus(@Param("status") ApplicationStatus status);

    @Query("SELECT e.companyName as enterpriseName, a.status as status, COUNT(a) as count " +
           "FROM Application a JOIN a.opening o JOIN o.enterprise e " +
           "GROUP BY e.companyName, a.status")
    List<EnterpriseStatusCountProjection> countApplicationsByStatusPerEnterprise();

    @Query("SELECT e.companyName as enterpriseName, COUNT(a) as count " +
           "FROM Application a JOIN a.opening o JOIN o.enterprise e " +
           "GROUP BY e.companyName")
    List<EnterpriseMetricCountProjection> countApplicationsPerEnterprise();

    @Query("SELECT o.title as openingName, e.companyName as enterpriseName, COUNT(a) as count " +
           "FROM Application a JOIN a.opening o JOIN o.enterprise e " +
           "GROUP BY o.title, e.companyName ORDER BY COUNT(a) DESC")
    List<OpeningApplicationCountProjection> countApplicationsPerOpening();

    @Query(value = "SELECT e.company_name as enterpriseName, AVG(EXTRACT(EPOCH FROM (a.updated_at - a.created_at))) / 86400.0 as averageProcessingTimeDays " +
                   "FROM applications a " +
                   "JOIN openings o ON a.opening_id = o.id " +
                   "JOIN enterprise_accounts e ON o.enterprise_id = e.id " +
                   "WHERE a.status IN ('SELECTED', 'REJECTED') " +
                   "GROUP BY e.company_name", nativeQuery = true)
    List<ProcessingTimeProjection> calculateAverageProcessingTimePerEnterprise();

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (a.updated_at - a.created_at))) / 86400.0 " +
                   "FROM applications a " +
                   "WHERE a.status IN ('SELECTED', 'REJECTED')", nativeQuery = true)
    Double calculateOverallAverageProcessingTimeDays();

    List<Application> findByCurrentHandlerId(UUID handlerId);

    List<Application> findByCurrentHandlerIdIsNull();

    int countByCurrentHandlerIdIsNotNull();
}
