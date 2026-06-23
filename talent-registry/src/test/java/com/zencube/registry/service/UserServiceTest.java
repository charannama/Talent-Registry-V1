package com.zencube.registry.service;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.user.dto.CreateUserRequest;
import com.zencube.registry.user.dto.UpdateUserRequest;
import com.zencube.registry.user.dto.UserAdminResponse;
import com.zencube.registry.user.mapper.UserMapper;
import com.zencube.registry.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("+1234567890")
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();
        sampleUser.setId(userId);
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createUser: success with password encoding")
    void createUser_success() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("new@example.com")
                .password("Secure@123")
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secure@123")).thenReturn("hashed-pwd");
        
        User savedUser = User.builder()
                .email("new@example.com")
                .firstName("New")
                .lastName("User")
                .passwordHash("hashed-pwd")
                .status(UserStatus.PENDING_VERIFICATION)
                .authProvider(AuthProvider.LOCAL)
                .build();
        savedUser.setId(UUID.randomUUID());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserAdminResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("new@example.com", response.getEmail());
        assertEquals("New", response.getFirstName());
        verify(passwordEncoder).encode("Secure@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: fails when email already exists")
    void createUser_duplicateEmail() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("test@example.com")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getUser: success when active")
    void getUser_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        UserAdminResponse response = userService.getUser(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("getUser: fails when not found")
    void getUser_notFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    @DisplayName("getUser: fails when soft-deleted")
    void getUser_softDeleted() {
        sampleUser.setDeleted(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    @DisplayName("getUserByEmail: success")
    void getUserByEmail_success() {
        when(userRepository.findByEmailAndDeletedFalse("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserAdminResponse response = userService.getUserByEmail("test@example.com");

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("getAllUsers: returns only active users")
    void getAllUsers_success() {
        User deletedUser = User.builder().email("del@example.com").build();
        deletedUser.setDeleted(true);

        when(userRepository.findAll()).thenReturn(List.of(sampleUser, deletedUser));

        List<UserAdminResponse> responses = userService.getAllUsers();

        assertEquals(1, responses.size());
        assertEquals("test@example.com", responses.get(0).getEmail());
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateUser: success with partial updates")
    void updateUser_success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .status(UserStatus.SUSPENDED)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserAdminResponse response = userService.updateUser(userId, request);

        assertEquals("Updated", sampleUser.getFirstName());
        assertEquals("User", sampleUser.getLastName()); // unmodified
        assertEquals(UserStatus.SUSPENDED, sampleUser.getStatus());
        
        assertEquals("Updated", response.getFirstName());
        assertEquals(UserStatus.SUSPENDED, response.getStatus());
        verify(userRepository).save(sampleUser);
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("deleteUser: success soft deletes user")
    void deleteUser_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        userService.deleteUser(userId);

        assertTrue(sampleUser.isDeleted());
        assertNotNull(sampleUser.getDeletedAt());
        assertEquals("system", sampleUser.getDeletedBy());
        verify(userRepository).save(sampleUser);
    }
}
