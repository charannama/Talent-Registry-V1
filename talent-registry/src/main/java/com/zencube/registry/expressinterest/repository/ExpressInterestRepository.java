package com.zencube.registry.expressinterest.repository;

import com.zencube.registry.expressinterest.entity.ExpressInterest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpressInterestRepository extends JpaRepository<ExpressInterest, UUID> {

    List<ExpressInterest> findByEnterpriseId(UUID enterpriseId);
    
    List<ExpressInterest> findByStudentId(UUID studentId);

    Optional<ExpressInterest> findByEnterpriseIdAndStudentIdAndOpeningId(UUID enterpriseId, UUID studentId, UUID openingId);

    Page<ExpressInterest> findByEnterpriseIdAndStage(UUID enterpriseId, com.zencube.registry.expressinterest.enums.InterestStage stage, Pageable pageable);
}
