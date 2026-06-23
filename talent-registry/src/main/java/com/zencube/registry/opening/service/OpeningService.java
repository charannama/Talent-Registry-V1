package com.zencube.registry.opening.service;

import com.zencube.registry.opening.dto.request.CreateOpeningRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;

import java.util.List;
import java.util.UUID;

public interface OpeningService {

    OpeningResponse createOpening(CreateOpeningRequest request);

    OpeningResponse getOpening(UUID id);

    List<OpeningResponse> listOpenings(UUID enterpriseId);

    OpeningResponse submitOpening(UUID id);

    OpeningResponse approveOpening(UUID openingId);

    OpeningResponse updateDraft(UUID id, com.zencube.registry.opening.dto.request.UpdateOpeningRequest request);

    OpeningResponse rejectOpening(UUID id, com.zencube.registry.opening.dto.request.RejectionRequest request);

    OpeningResponse requestRevision(UUID id, com.zencube.registry.opening.dto.request.RequestRevisionRequest request);

    com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse browseOpenings(org.springframework.data.domain.Pageable pageable);

    com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse searchOpenings(com.zencube.registry.opening.dto.request.OpeningSearchCriteria criteria, org.springframework.data.domain.Pageable pageable);

    com.zencube.registry.opening.dto.response.ResubmitOpeningResponse resubmitOpening(UUID id);

    com.zencube.registry.opening.dto.response.CloseOpeningResponse closeOpening(UUID id, com.zencube.registry.opening.dto.request.CloseOpeningRequest request);

    OpeningResponse archiveOpening(UUID id);

    com.zencube.registry.opening.dto.response.PaginatedOpeningResponse listEnterpriseOpenings(UUID enterpriseId, org.springframework.data.domain.Pageable pageable);

    com.zencube.registry.opening.dto.response.PaginatedOpeningResponse listMyEnterpriseOpenings(org.springframework.data.domain.Pageable pageable);

    com.zencube.registry.opening.dto.response.PaginatedOpeningResponse listPendingOpenings(org.springframework.data.domain.Pageable pageable);
}
