package com.zencube.registry.enterprise.repository;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EnterpriseAccountSpecification {

    private EnterpriseAccountSpecification() {}

    public static Specification<EnterpriseAccount> filterBy(EnterpriseOnboardingStatus status, String companyName) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("onboardingStatus"), status));
            }

            if (companyName != null && !companyName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
