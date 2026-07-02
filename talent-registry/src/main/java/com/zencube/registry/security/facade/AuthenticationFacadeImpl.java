package com.zencube.registry.security.facade;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmailAndDeletedFalse(auth.getName())
                .orElse(null);
    }

    @Override
    public UUID getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
