package com.zencube.registry.opening.specification;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.OpeningSearchCriteria;
import com.zencube.registry.opening.enums.OpeningStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;
import com.zencube.registry.application.entity.Application;

public class OpeningSpecification {

    public static Specification<Opening> search(OpeningSearchCriteria criteria) {
        return search(criteria, null, null);
    }

    public static Specification<Opening> search(OpeningSearchCriteria criteria, com.zencube.registry.eligibility.dto.StudentEligibilityResponse eligibility, UUID studentProfileId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Implicit security and visibility filters
            predicates.add(cb.equal(root.get("status"), OpeningStatus.LIVE));
            predicates.add(cb.equal(root.get("deleted"), false));

            // Deadline must be in the future (or null, depending on business rules, but requirement says "deadline has not passed")
            Predicate deadlineNotPassed = cb.or(
                    cb.isNull(root.get("applicationDeadline")),
                    cb.greaterThan(root.get("applicationDeadline"), Instant.now())
            );
            predicates.add(deadlineNotPassed);

            // Enterprise must be approved and active
            // Optimize by creating a join once if we need to filter by enterprise
            Join<Opening, EnterpriseAccount> enterpriseJoin = root.join("enterprise", JoinType.INNER);
            predicates.add(cb.equal(enterpriseJoin.get("onboardingStatus"), EnterpriseOnboardingStatus.APPROVED));
            predicates.add(cb.equal(enterpriseJoin.get("accountActive"), true));
            predicates.add(cb.equal(enterpriseJoin.get("deleted"), false));

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // Domain filter
            if (criteria.getDomain() != null && !criteria.getDomain().isBlank()) {
                predicates.add(cb.equal(root.get("domain"), criteria.getDomain()));
            }

            // Job Type filter
            if (criteria.getJobType() != null) {
                predicates.add(cb.equal(root.get("jobType"), criteria.getJobType()));
            }

            // Work Mode filter
            if (criteria.getWorkMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), criteria.getWorkMode()));
            }

            // Featured filter
            if (criteria.getFeatured() != null) {
                predicates.add(cb.equal(root.get("featured"), criteria.getFeatured()));
            }

            // Company Name filter (case-insensitive partial match)
            if (criteria.getCompany() != null && !criteria.getCompany().isBlank()) {
                String companyPattern = "%" + criteria.getCompany().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(enterpriseJoin.get("companyName")), companyPattern));
            }

            // Salary Overlap detection
            // Two ranges overlap if: (start1 <= end2) AND (end1 >= start2)
            // Range 1 (Opening): root.get("salaryMin"), root.get("salaryMax")
            // Range 2 (Search): criteria.getSalaryMin(), criteria.getSalaryMax()
            if (criteria.getSalaryMin() != null || criteria.getSalaryMax() != null) {
                Predicate salaryOverlap = cb.conjunction();
                
                if (criteria.getSalaryMin() != null) {
                    // Opening Max >= Search Min
                    // Handle cases where Opening Max is null
                    Predicate maxCondition = cb.or(
                            cb.isNull(root.get("salaryMax")),
                            cb.greaterThanOrEqualTo(root.get("salaryMax"), criteria.getSalaryMin())
                    );
                    salaryOverlap = cb.and(salaryOverlap, maxCondition);
                }
                
                if (criteria.getSalaryMax() != null) {
                    // Opening Min <= Search Max
                    // Handle cases where Opening Min is null
                    Predicate minCondition = cb.or(
                            cb.isNull(root.get("salaryMin")),
                            cb.lessThanOrEqualTo(root.get("salaryMin"), criteria.getSalaryMax())
                    );
                    salaryOverlap = cb.and(salaryOverlap, minCondition);
                }
                
                predicates.add(salaryOverlap);
            }

            // Graduation Year filter (matches substring in csv or similar format)
            if (criteria != null && criteria.getGraduationYear() != null && !criteria.getGraduationYear().isBlank()) {
                String gradPattern = "%" + criteria.getGraduationYear() + "%";
                predicates.add(cb.like(root.get("graduationYears"), gradPattern));
            }

            // --- ELIGIBILITY FILTER ---
            if (eligibility != null && studentProfileId != null && criteria != null && Boolean.TRUE.equals(criteria.getEligibleOnly())) {
                
                // 1. If max applications reached, or NO_PROJECT, student is eligible for NOTHING.
                if (eligibility.isMaxApplicationsReached() || eligibility.getEligibilityLevel() == com.zencube.registry.eligibility.enums.EligibilityLevel.NO_PROJECT) {
                    predicates.add(cb.disjunction()); // 1=0, always false
                } else {
                    // 2. Filter by permitted JobTypes
                    if (eligibility.getPermittedJobTypes() != null && !eligibility.getPermittedJobTypes().isEmpty()) {
                        predicates.add(root.get("jobType").in(eligibility.getPermittedJobTypes()));
                    } else {
                        predicates.add(cb.disjunction()); // Should not happen if not NO_PROJECT, but safe fallback
                    }

                    // 3. Exclude jobs already applied to (Subquery)
                    Subquery<UUID> appliedSubquery = query.subquery(UUID.class);
                    Root<Application> appRoot = appliedSubquery.from(Application.class);
                    appliedSubquery.select(appRoot.get("opening").get("id"));
                    appliedSubquery.where(cb.and(
                            cb.equal(appRoot.get("profile").get("id"), studentProfileId),
                            cb.equal(appRoot.get("deleted"), false)
                    ));
                    
                    predicates.add(cb.not(root.get("id").in(appliedSubquery)));
                    
                    // 4. Graduation year exact match check if student's graduation year is known
                    if (eligibility.getGraduationYear() != null) {
                        String studentGradPattern = "%" + eligibility.getGraduationYear() + "%";
                        predicates.add(cb.or(
                                cb.isNull(root.get("graduationYears")),
                                cb.equal(root.get("graduationYears"), ""),
                                cb.like(root.get("graduationYears"), studentGradPattern)
                        ));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
