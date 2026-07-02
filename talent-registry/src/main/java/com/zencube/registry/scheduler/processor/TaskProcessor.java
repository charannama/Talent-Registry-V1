package com.zencube.registry.scheduler.processor;

import com.zencube.registry.scheduler.entity.ScheduledTask;

public interface TaskProcessor {
    boolean supports(String taskType);
    void process(ScheduledTask task);
}
