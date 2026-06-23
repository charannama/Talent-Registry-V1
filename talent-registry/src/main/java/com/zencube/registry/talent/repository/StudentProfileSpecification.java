package com.zencube.registry.talent.repository;

import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.entity.StudentProject;
import com.zencube.registry.profile.entity.StudentSkill;
import com.zencube.registry.profile.enums.ProjectType;
import com.zencube.registry.talent.dto.request.TalentSearchRequest;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class StudentProfileSpecification {

    public static Specification<StudentProfile> build(TalentSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Visibility rules
            predicates.add(cb.isTrue(root.get("profileVisible")));
            predicates.add(cb.isFalse(root.get("suspended")));
            predicates.add(cb.isTrue(root.get("searchable")));

            // Filters
            // Global Rule: Talent Search only returns profiles with at least 1 project
            Subquery<Long> globalProjectSub = query.subquery(Long.class);
            Root<StudentProject> globalProjectRoot = globalProjectSub.from(StudentProject.class);
            globalProjectSub.select(cb.count(globalProjectRoot));
            globalProjectSub.where(cb.equal(globalProjectRoot.get("profile"), root));
            predicates.add(cb.greaterThan(globalProjectSub, 0L));

            if (request.getQualifiedOnly() != null && request.getQualifiedOnly()) {
                predicates.add(cb.isTrue(root.get("talentQualified")));
            }

            if (request.getGraduationYears() != null && !request.getGraduationYears().isEmpty()) {
                predicates.add(root.get("graduationYear").in(request.getGraduationYears()));
            }

            if (request.getDiscipline() != null && !request.getDiscipline().isBlank()) {
                predicates.add(cb.equal(root.get("discipline"), request.getDiscipline()));
            }

            if (request.getInstitution() != null && !request.getInstitution().isBlank()) {
                predicates.add(cb.equal(root.get("institution"), request.getInstitution()));
            }

            if (request.getMinimumProjectType() != null) {
                List<ProjectType> validTypes = getValidTypes(request.getMinimumProjectType());
                predicates.add(root.get("highestProjectType").in(validTypes));
            }

            if (request.getProjectDomain() != null && !request.getProjectDomain().isBlank()) {
                Subquery<Long> projectSub = query.subquery(Long.class);
                Root<StudentProject> projectRoot = projectSub.from(StudentProject.class);
                projectSub.select(cb.count(projectRoot));
                projectSub.where(
                        cb.equal(projectRoot.get("profile"), root),
                        cb.equal(projectRoot.get("domain"), request.getProjectDomain())
                );
                predicates.add(cb.greaterThan(projectSub, 0L));
            }

            if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                for (String skill : request.getSkills()) {
                    Subquery<Long> skillSub = query.subquery(Long.class);
                    Root<StudentSkill> skillRoot = skillSub.from(StudentSkill.class);
                    skillSub.select(cb.count(skillRoot));
                    skillSub.where(
                            cb.equal(skillRoot.get("profile"), root),
                            cb.equal(cb.lower(skillRoot.get("skillName")), skill.toLowerCase())
                    );
                    predicates.add(cb.greaterThan(skillSub, 0L));
                }
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<ProjectType> getValidTypes(ProjectType minType) {
        if (minType == ProjectType.CAPSTONE) {
            return List.of(ProjectType.CAPSTONE);
        } else if (minType == ProjectType.MINI) {
            return List.of(ProjectType.MINI, ProjectType.CAPSTONE);
        } else {
            return List.of(ProjectType.NANO, ProjectType.MINI, ProjectType.CAPSTONE);
        }
    }
}
