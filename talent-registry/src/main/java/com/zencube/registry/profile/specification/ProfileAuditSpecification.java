package com.zencube.registry.profile.specification;

import com.zencube.registry.profile.entity.ProfileAccessAudit;
import com.zencube.registry.profile.enums.AccessResult;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class ProfileAuditSpecification {

    public static Specification<ProfileAccessAudit> hasViewerUserId(UUID viewerUserId) {
        return (root, query, cb) -> viewerUserId == null ? cb.conjunction() : cb.equal(root.get("viewerUserId"), viewerUserId);
    }

    public static Specification<ProfileAccessAudit> hasTargetUserId(UUID targetUserId) {
        return (root, query, cb) -> targetUserId == null ? cb.conjunction() : cb.equal(root.get("targetUserId"), targetUserId);
    }

    public static Specification<ProfileAccessAudit> hasAccessResult(AccessResult accessResult) {
        return (root, query, cb) -> accessResult == null ? cb.conjunction() : cb.equal(root.get("accessResult"), accessResult);
    }

    public static Specification<ProfileAccessAudit> createdAfter(Instant startDate) {
        return (root, query, cb) -> startDate == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<ProfileAccessAudit> createdBefore(Instant endDate) {
        return (root, query, cb) -> endDate == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }

    public static Specification<ProfileAccessAudit> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}
