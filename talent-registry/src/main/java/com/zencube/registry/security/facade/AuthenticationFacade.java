package com.zencube.registry.security.facade;

import com.zencube.registry.auth.entity.User;
import java.util.UUID;

public interface AuthenticationFacade {
    User getCurrentUser();
    UUID getCurrentUserId();
}
