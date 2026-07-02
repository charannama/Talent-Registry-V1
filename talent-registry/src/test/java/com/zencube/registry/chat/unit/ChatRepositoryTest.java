package com.zencube.registry.chat.repository;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.chat.entity.ChatMessage;
import com.zencube.registry.chat.entity.ChatParticipant;
import com.zencube.registry.chat.entity.ChatThread;
import com.zencube.registry.chat.enums.ThreadType;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private ChatThreadRepository chatThreadRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = createUser("usera@test.com", RoleType.STUDENT);
        userB = createUser("userb@test.com", RoleType.HR_STAFF);
    }

    private User createUser(String email, RoleType roleType) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Role role = roleRepository.findByNameAndDeletedFalse(roleType.name()).orElse(null);
        if (role == null) {
            role = new Role();
            role.setName(roleType.name());
            role.setRoleType(roleType);
            role = roleRepository.save(role);
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        return userRepository.save(user);
    }

    @Test
    void testChatThreadContextQueries() {
        UUID contextId = UUID.randomUUID();
        
        ChatThread thread = ChatThread.builder()
                .threadType(ThreadType.HR_STUDENT)
                .contextableType("Application")
                .contextableId(contextId)
                .creator(userB)
                .isArchived(false)
                .build();
        chatThreadRepository.save(thread);

        Optional<ChatThread> found = chatThreadRepository.findByContextableTypeAndContextableId("Application", contextId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(thread.getId());

        boolean exists = chatThreadRepository.existsByContextableTypeAndContextableId("Application", contextId);
        assertThat(exists).isTrue();
    }

    @Test
    void testUnreadCountLogic() {
        ChatThread thread = ChatThread.builder()
                .threadType(ThreadType.HR_STUDENT)
                .creator(userB)
                .isArchived(false)
                .build();
        thread = chatThreadRepository.save(thread);

        ChatParticipant participantA = ChatParticipant.builder()
                .thread(thread)
                .user(userA)
                .joinedAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build();
        participantA = chatParticipantRepository.save(participantA);

        ChatParticipant participantB = ChatParticipant.builder()
                .thread(thread)
                .user(userB)
                .joinedAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build();
        participantB = chatParticipantRepository.save(participantB);

        // B sends 3 messages
        for (int i = 0; i < 3; i++) {
            ChatMessage msg = ChatMessage.builder()
                    .thread(thread)
                    .sender(userB)
                    .content("Msg " + i)
                    .sentAt(Instant.now().minus(1, ChronoUnit.DAYS))
                    .build();
            chatMessageRepository.save(msg);
        }

        // 1. Initial Unread Count (lastReadAt is null)
        long unreadA = chatMessageRepository.countAllUnreadMessages(thread.getId(), userA.getId());
        assertThat(unreadA).isEqualTo(3);

        // User B's unread should be 0 because B sent them
        long unreadB = chatMessageRepository.countAllUnreadMessages(thread.getId(), userB.getId());
        assertThat(unreadB).isEqualTo(0);

        // 2. Mark A as read
        Instant readTime = Instant.now();
        chatParticipantRepository.updateLastReadAt(participantA.getId(), readTime);

        // 3. Unread is 0
        long unreadAfterRead = chatMessageRepository.countUnreadMessages(thread.getId(), userA.getId(), readTime);
        assertThat(unreadAfterRead).isEqualTo(0);

        // 4. B sends another message
        ChatMessage newMsg = ChatMessage.builder()
                .thread(thread)
                .sender(userB)
                .content("New Msg")
                .sentAt(Instant.now())
                .build();
        chatMessageRepository.save(newMsg);

        // 5. Unread is 1
        long finalUnread = chatMessageRepository.countUnreadMessages(thread.getId(), userA.getId(), readTime);
        assertThat(finalUnread).isEqualTo(1);
    }
}



