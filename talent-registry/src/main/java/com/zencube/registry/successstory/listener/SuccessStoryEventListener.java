package com.zencube.registry.successstory.listener;

import com.zencube.registry.application.entity.Application;
import com.zencube.registry.application.event.ApplicationStatusChangedEvent;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.successstory.service.SuccessStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuccessStoryEventListener {

    private final SuccessStoryService successStoryService;
    private final ApplicationRepository applicationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationSelected(ApplicationStatusChangedEvent event) {
        if (event.getNewStatus() == ApplicationStatus.SELECTED) {
            log.info("Application {} reached SELECTED. Generating Success Story.", event.getApplicationId());
            try {
                Application application = applicationRepository.findById(event.getApplicationId()).orElse(null);
                if (application != null) {
                    successStoryService.createFromApplication(application);
                }
            } catch (Exception e) {
                log.error("Failed to generate success story from application event", e);
            }
        }
    }
}
