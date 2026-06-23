package com.zencube.registry.auth.service;

import com.zencube.registry.auth.entity.User;
import java.util.Map;

public interface ZenCubeProfileSyncService {
    void syncStudentProfileAsync(User user, Map<String, Object> oauth2Attributes);
}
