package com.zencube.registry.common.seeder;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class TestUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting Test User Seeding...");
        String password = "Secure@123";
        String encodedPassword = passwordEncoder.encode(password);

        seedUser("emily.carter81@example.test", "Emily", "Carter", encodedPassword, RoleType.STUDENT);
        seedUser("john.anderson@example.test", "John", "Anderson", encodedPassword, RoleType.STUDENT);
        seedUser("hr.manager@company.test", "HR", "Manager", encodedPassword, RoleType.HR_STAFF);
        seedUser("enterprise.admin@company.test", "Enterprise", "Admin", encodedPassword, RoleType.ENTERPRISE_ADMIN);
        
        // Also ensure admin from Postman works
        seedUser("admin@zencube.com", "Admin", "User", passwordEncoder.encode("Password@123"), RoleType.ADMIN);
        
        log.info("Test User Seeding Completed.");
    }

    private void seedUser(String email, String firstName, String lastName, String passwordHash, RoleType roleType) {
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            return;
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .status(UserStatus.ACTIVE)
                .build();
                
        user = userRepository.save(user);

        Role role = roleRepository.findByRoleTypeAndDeletedFalse(roleType)
                .orElse(null);

        if (role != null) {
            UserRole userRole = new UserRole(user, role);
            userRoleRepository.save(userRole);
            log.info("Seeded test user: {} with role: {}", email, roleType);
        } else {
            log.warn("Role {} not found, user {} seeded without role.", roleType, email);
        }
    }
}
