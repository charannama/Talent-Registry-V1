package com.zencube.registry.auth.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.service.ZenCubeProfileSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ZenCubeProfileSyncServiceImpl implements ZenCubeProfileSyncService {

    @Async
    @Override
    public void syncStudentProfileAsync(User user, Map<String, Object> oauth2Attributes) {
        log.info("Starting background profile sync for user: {}", user.getEmail());
        
        try {
            // Extract attributes from the token response
            String studentId = (String) oauth2Attributes.get("studentId");
            String program = (String) oauth2Attributes.get("program");
            String branch = (String) oauth2Attributes.get("branch");
            String graduationYear = (String) oauth2Attributes.get("graduationYear");
            
            log.info("Extracted ZenCube Student Profile: ID={}, Program={}, Branch={}, GradYear={}", 
                studentId, program, branch, graduationYear);
                
            // TODO: In Phase 14.2, this will save the Student Profile entity 
            // and trigger any cache invalidation required.
            
            log.info("Profile sync completed for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to sync student profile for user: {}", user.getEmail(), e);
            // Async failure does not break the login flow
        }
    }
}
