package com.zencube.registry.service;

import com.zencube.registry.auth.dto.role.CreateRoleRequest;
import com.zencube.registry.auth.dto.role.RoleResponse;
import com.zencube.registry.auth.dto.role.UpdateRoleRequest;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.service.impl.RoleServiceImpl;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void shouldCreateRole() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("ADMIN");
        request.setRoleType(RoleType.ADMIN);

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ADMIN");
        role.setRoleType(RoleType.ADMIN);

        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponse response = roleService.createRole(request);

        assertNotNull(response);
        assertEquals("ADMIN", response.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldGetRoleById() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setId(id);
        role.setName("ADMIN");
        role.setRoleType(RoleType.ADMIN);

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        RoleResponse response = roleService.getRole(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("ADMIN", response.getName());
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        when(roleRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> roleService.getRole(UUID.randomUUID())
        );
    }

    @Test
    void shouldGetAllRoles() {
        Role role1 = new Role();
        role1.setId(UUID.randomUUID());
        role1.setName("ADMIN");
        role1.setRoleType(RoleType.ADMIN);

        Role role2 = new Role();
        role2.setId(UUID.randomUUID());
        role2.setName("STUDENT");
        role2.setRoleType(RoleType.STUDENT);

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

        List<RoleResponse> responses = roleService.getAllRoles();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(roleRepository).findAll();
    }

    @Test
    void shouldUpdateRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setId(id);
        role.setName("OLD_NAME");
        role.setRoleType(RoleType.VIEWER);

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("NEW_NAME");
        request.setRoleType(RoleType.ADMIN);
        request.setDescription("New Description");

        Role updatedRole = new Role();
        updatedRole.setId(id);
        updatedRole.setName("NEW_NAME");
        updatedRole.setRoleType(RoleType.ADMIN);
        updatedRole.setDescription("New Description");

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        RoleResponse response = roleService.updateRole(id, request);

        assertNotNull(response);
        assertEquals("NEW_NAME", response.getName());
        assertEquals(RoleType.ADMIN, response.getRoleType());
        assertEquals("New Description", response.getDescription());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldDeleteRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setId(id);
        role.setName("ADMIN");
        role.setRoleType(RoleType.ADMIN);

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        roleService.deleteRole(id);

        assertTrue(role.isDeleted());
        assertNotNull(role.getDeletedAt());
        assertEquals("system", role.getDeletedBy());
        verify(roleRepository).save(role);
    }
}
