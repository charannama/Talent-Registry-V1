package com.zencube.registry.application.mapper;

import com.zencube.registry.application.dto.response.PendingApplicationResponse;
import com.zencube.registry.application.entity.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public PendingApplicationResponse toPendingResponse(Application application) {
        if (application == null) {
            return null;
        }
        
        return PendingApplicationResponse.builder()
                .applicationId(application.getId())
                .studentId(application.getProfile() != null && application.getProfile().getUser() != null ? application.getProfile().getUser().getId() : null)
                .studentName(application.getProfile() != null && application.getProfile().getUser() != null ? application.getProfile().getUser().getDisplayName() : null)
                .openingId(application.getOpening() != null ? application.getOpening().getId() : null)
                .openingTitle(application.getOpening() != null ? application.getOpening().getTitle() : null)
                .appliedAt(application.getAppliedAt() != null ? application.getAppliedAt() : application.getCreatedAt())
                .status(application.getStatus())
                .currentHandlerId(application.getCurrentHandlerId())
                .build();
    }

    public com.zencube.registry.application.dto.response.EnterpriseApplicationResponse toEnterpriseResponse(Application application) {
        if (application == null) {
            return null;
        }

        String resumeUrl = null;
        String resumeFileName = null;
        if (application.getResume() != null) {
            resumeUrl = application.getResume().getStoragePath();
            resumeFileName = application.getResume().getFilename();
        }

        return com.zencube.registry.application.dto.response.EnterpriseApplicationResponse.builder()
                .applicationId(application.getId())
                .studentId(application.getProfile() != null && application.getProfile().getUser() != null ? application.getProfile().getUser().getId() : null)
                .studentName(application.getProfile() != null && application.getProfile().getUser() != null ? application.getProfile().getUser().getDisplayName() : null)
                .studentEmail(application.getProfile() != null && application.getProfile().getUser() != null ? application.getProfile().getUser().getEmail() : null)
                .studentPhone(application.getProfile() != null && application.getProfile().getUser() != null ? application.getProfile().getUser().getPhone() : null)
                .openingId(application.getOpening() != null ? application.getOpening().getId() : null)
                .openingTitle(application.getOpening() != null ? application.getOpening().getTitle() : null)
                .status(application.getStatus())
                .forwardedAt(application.getForwardedAt())
                .lastStageUpdatedAt(application.getLastStageUpdatedAt() != null ? application.getLastStageUpdatedAt() : application.getUpdatedAt())
                .resumeUrl(resumeUrl)
                .resumeFileName(resumeFileName)
                .currentHandlerId(application.getCurrentHandlerId())
                .build();
    }
}
