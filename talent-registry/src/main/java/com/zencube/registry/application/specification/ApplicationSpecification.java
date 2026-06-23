package com.zencube.registry.application.specification;

import com.zencube.registry.application.entity.Application;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.profile.entity.StudentProfile;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    @SuppressWarnings("unchecked")
    public static Specification<Application> getPendingApplications(String search) {
        return (root, query, criteriaBuilder) -> {
            
            Join<Application, StudentProfile> profileJoin;
            Join<StudentProfile, User> userJoin;
            Join<Application, Opening> openingJoin;

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                Fetch<Application, StudentProfile> profileFetch = root.fetch("profile", JoinType.INNER);
                Fetch<StudentProfile, User> userFetch = profileFetch.fetch("user", JoinType.INNER);
                Fetch<Application, Opening> openingFetch = root.fetch("opening", JoinType.INNER);
                
                profileJoin = (Join<Application, StudentProfile>) profileFetch;
                userJoin = (Join<StudentProfile, User>) userFetch;
                openingJoin = (Join<Application, Opening>) openingFetch;
            } else {
                profileJoin = root.join("profile", JoinType.INNER);
                userJoin = profileJoin.join("user", JoinType.INNER);
                openingJoin = root.join("opening", JoinType.INNER);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), ApplicationStatus.APPLIED));

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                
                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), searchPattern);
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), searchPattern);
                Predicate emailMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("email")), searchPattern);
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(openingJoin.get("title")), searchPattern);

                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch, emailMatch, titleMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SuppressWarnings("unchecked")
    public static Specification<Application> getEnterpriseApplications(java.util.UUID openingId, String search, String status) {
        return (root, query, criteriaBuilder) -> {
            Join<Application, StudentProfile> profileJoin;
            Join<StudentProfile, User> userJoin;
            Join<Application, Opening> openingJoin;

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                Fetch<Application, StudentProfile> profileFetch = root.fetch("profile", JoinType.INNER);
                Fetch<StudentProfile, User> userFetch = profileFetch.fetch("user", JoinType.INNER);
                Fetch<Application, Opening> openingFetch = root.fetch("opening", JoinType.INNER);
                root.fetch("resume", JoinType.LEFT);
                
                profileJoin = (Join<Application, StudentProfile>) profileFetch;
                userJoin = (Join<StudentProfile, User>) userFetch;
                openingJoin = (Join<Application, Opening>) openingFetch;
            } else {
                profileJoin = root.join("profile", JoinType.INNER);
                userJoin = profileJoin.join("user", JoinType.INNER);
                openingJoin = root.join("opening", JoinType.INNER);
                root.join("resume", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(openingJoin.get("id"), openingId));

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), ApplicationStatus.valueOf(status.toUpperCase())));
            } else {
                // Default: Enterprise can view FORWARDED, INTERVIEW_SCHEDULED, SELECTED, REJECTED
                CriteriaBuilder.In<ApplicationStatus> inClause = criteriaBuilder.in(root.get("status"));
                inClause.value(ApplicationStatus.FORWARDED);
                inClause.value(ApplicationStatus.INTERVIEW_SCHEDULED);
                inClause.value(ApplicationStatus.SELECTED);
                inClause.value(ApplicationStatus.REJECTED);
                predicates.add(inClause);
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                
                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), searchPattern);
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), searchPattern);
                Predicate emailMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("email")), searchPattern);

                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch, emailMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
