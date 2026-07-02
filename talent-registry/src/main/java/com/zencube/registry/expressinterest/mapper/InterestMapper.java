package com.zencube.registry.expressinterest.mapper;

import com.zencube.registry.expressinterest.dto.FormalRequestResponse;
import com.zencube.registry.expressinterest.dto.InterestResponse;
import com.zencube.registry.expressinterest.entity.ExpressInterest;
import org.springframework.stereotype.Component;

@Component
public class InterestMapper {

    public InterestResponse toResponse(ExpressInterest interest) {
        if (interest == null) return null;
        
        return new InterestResponse(
            interest.getId(),
            interest.getEnterprise() != null ? interest.getEnterprise().getId() : null,
            interest.getStudent() != null ? interest.getStudent().getId() : null,
            interest.getOpening() != null ? interest.getOpening().getId() : null,
            interest.getStage(),
            interest.getRequestedAt(),
            interest.getCreatedAt()
        );
    }

    public FormalRequestResponse toFormalRequestResponse(ExpressInterest interest) {
        if (interest == null) return null;

        return new FormalRequestResponse(
            interest.getId(),
            interest.getEnterprise() != null ? interest.getEnterprise().getId() : null,
            interest.getStudent() != null ? interest.getStudent().getId() : null,
            interest.getOpening() != null ? interest.getOpening().getId() : null,
            interest.getRequestedAt()
        );
    }
}
