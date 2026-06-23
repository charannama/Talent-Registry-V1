package com.zencube.registry.eligibility.service;

import com.zencube.registry.eligibility.dto.StudentEligibilityResponse;

import java.util.UUID;

public interface EligibilityService {

    /**
     * Computes the eligibility matrix for a given user.
     * @param userId The ID of the student user
     * @return StudentEligibilityResponse encapsulating limits and levels
     */
    StudentEligibilityResponse getStudentEligibility(UUID userId);

}
