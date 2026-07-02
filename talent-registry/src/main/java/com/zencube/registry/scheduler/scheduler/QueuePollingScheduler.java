package com.zencube.registry.scheduler.scheduler;

import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueuePollingScheduler {

    private final TaskSchedulerService taskSchedulerService;
    private final ConfigService configService;

    // Use a fixed delay string so it can be overridden if needed, or we just rely on Spring's properties.
    // By default, polls every 30 seconds.
    @Scheduled(fixedDelayString = "${scheduler.queue.poll-delay:30000}")
    public void pollQueue() {
        log.trace("Polling job queue for pending tasks...");
        try {
            // Optional dynamic check if polling is enabled via System Config
            Boolean enabled = true;
            try {
                enabled = configService.get("QUEUE.POLLING_ENABLED", Boolean.class);
            } catch (Exception e) {
                // Default to true
            }

            if (Boolean.TRUE.equals(enabled)) {
                taskSchedulerService.processPendingTasks();
            }
        } catch (Exception e) {
            log.error("Error occurred during queue polling: {}", e.getMessage());
        }
    }
}
