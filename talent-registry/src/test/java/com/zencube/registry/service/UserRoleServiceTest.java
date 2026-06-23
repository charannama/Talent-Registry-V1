package com.zencube.registry.service;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.userrole.dto.CreateUserRoleRequest;
import com.zencube.registry.userrole.dto.UserRoleResponse;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.mapper.UserRoleMapper;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.userrole.service.impl.UserRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleServiceImpl Unit Tests")
class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Spy
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private UUID userId;
    private UUID roleId;
    private UUID mappingId;
    private User user;
    private Role role;
    private UserRole mapping;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        mappingId = UUID.randomUUID();

        user = User.builder().email("test@example.com").build();
        user.setId(userId);

        role = Role.builder().name("ADMIN").build();
        role.setId(roleId);

        mapping = UserRole.builder().user(user).role(role).build();
        mapping.setId(mappingId);
    }

    @Test
    @DisplayName("assignRoleToUser: success")
    void assignRoleToUser_success() {
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserAndRoleAndDeletedFalse(user, role)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(mapping);

        UserRoleResponse response = userRoleService.assignRoleToUser(request);

        assertNotNull(response);
        assertEquals(mappingId, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRoleName());
    }

    @Test
    @DisplayName("assignRoleToUser: throws ConflictException if duplicate")
    void assignRoleToUser_duplicate() {
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserAndRoleAndDeletedFalse(user, role)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userRoleService.assignRoleToUser(request));
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignRoleToUser: throws ResourceNotFoundException if user missing")
    void assignRoleToUser_userNotFound() {
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userRoleService.assignRoleToUser(request));
    }

    @Test
    @DisplayName("getRolesByUser: success")
    void getRolesByUser_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserAndDeletedFalse(user)).thenReturn(List.of(mapping));

        List<UserRoleResponse> responses = userRoleService.getRolesByUser(userId);

        assertEquals(1, responses.size());
        assertEquals("ADMIN", responses.get(0).getRoleName());
    }

    @Test
    @DisplayName("removeRoleFromUser: success")
    void removeRoleFromUser_success() {
        when(userRoleRepository.findById(mappingId)).thenReturn(Optional.of(mapping));

        userRoleService.removeRoleFromUser(mappingId);

        assertTrue(mapping.isDeleted());
        assertNotNull(mapping.getDeletedAt());
        assertEquals("system", mapping.getDeletedBy());
        verify(userRoleRepository).save(mapping);
    }
}
