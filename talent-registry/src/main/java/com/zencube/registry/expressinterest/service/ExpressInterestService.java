package com.zencube.registry.expressinterest.service;

import com.zencube.registry.expressinterest.dto.InterestResponse;
import com.zencube.registry.expressinterest.dto.FormalRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ExpressInterestService {

    InterestResponse bookmark(UUID enterpriseId, UUID studentId, UUID openingId);

    FormalRequestResponse formalRequest(UUID enterpriseId, UUID interestId);

    Page<InterestResponse> getMyBookmarks(UUID enterpriseId, Pageable pageable);

    Page<FormalRequestResponse> getMyFormalRequests(UUID enterpriseId, Pageable pageable);
}
