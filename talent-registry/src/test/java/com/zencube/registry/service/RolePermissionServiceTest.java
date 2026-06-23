package com.zencube.registry.service;

import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.repository.PermissionRepository;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.rolepermission.dto.CreateRolePermissionRequest;
import com.zencube.registry.rolepermission.dto.RolePermissionResponse;
import com.zencube.registry.rolepermission.entity.RolePermission;
import com.zencube.registry.rolepermission.mapper.RolePermissionMapper;
import com.zencube.registry.rolepermission.repository.RolePermissionRepository;
import com.zencube.registry.rolepermission.service.impl.RolePermissionServiceImpl;
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

/**
 * Unit tests for {@link RolePermissionServiceImpl}.
 *
 * <p>Uses Mockito to isolate the service from all JPA repositories.
 * The {@link RolePermissionMapper} is a real {@code @Spy} so mapping logic
 * is exercised in-process without loading the Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RolePermissionServiceImpl Unit Tests")
class RolePermissionServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Spy
    private RolePermissionMapper rolePermissionMapper;

    @InjectMocks
    private RolePermissionServiceImpl rolePermissionService;

    // ---- test fixtures ----
    private UUID roleId;
    private UUID permissionId;
    private UUID mappingId;
    private Role sampleRole;
    private Permission samplePermission;
    private RolePermission sampleMapping;

    @BeforeEach
    void setUp() {
        roleId       = UUID.randomUUID();
        permissionId = UUID.randomUUID();
        mappingId    = UUID.randomUUID();

        sampleRole = Role.builder()
                .name("Admin")
                .roleType(RoleType.ADMIN)
                .build();
        sampleRole.setId(roleId);

        samplePermission = Permission.builder()
                .name("View Students")
                .code("VIEW_STUDENTS")
                .build();
        samplePermission.setId(permissionId);

        sampleMapping = RolePermission.builder()
                .role(sampleRole)
                .permission(samplePermission)
                .build();
        sampleMapping.setId(mappingId);
    }

    // ------------------------------------------------------------------
    // ASSIGN — Happy Path
    // ------------------------------------------------------------------

    @Test
    @DisplayName("assignPermissionToRole_success: should create and return the mapping")
    void assignPermissionToRole_success() {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permissionId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(sampleRole));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));
        when(rolePermissionRepository.existsByRoleAndPermissionAndDeletedFalse(sampleRole, samplePermission))
                .thenReturn(false);
        when(rolePermissionRepository.save(any(RolePermission.class))).thenReturn(sampleMapping);

        RolePermissionResponse response = rolePermissionService.assignPermissionToRole(request);

        assertNotNull(response);
        assertEquals(mappingId, response.getId());
        assertEquals("Admin", response.getRoleName());
        assertEquals("VIEW_STUDENTS", response.getPermissionCode());
        verify(rolePermissionRepository).save(any(RolePermission.class));
    }

    // ------------------------------------------------------------------
    // ASSIGN — Duplicate Mapping
    // ------------------------------------------------------------------

    @Test
    @DisplayName("assignPermissionToRole_duplicateMapping: should throw ConflictException")
    void assignPermissionToRole_duplicateMapping() {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permissionId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(sampleRole));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));
        when(rolePermissionRepository.existsByRoleAndPermissionAndDeletedFalse(sampleRole, samplePermission))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> rolePermissionService.assignPermissionToRole(request));

        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    }

    // ------------------------------------------------------------------
    // ASSIGN — Role Not Found
    // ------------------------------------------------------------------

    @Test
    @DisplayName("assignPermissionToRole_roleNotFound: should throw ResourceNotFoundException")
    void assignPermissionToRole_roleNotFound() {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permissionId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rolePermissionService.assignPermissionToRole(request));

        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    }

    // ------------------------------------------------------------------
    // ASSIGN — Permission Not Found
    // ------------------------------------------------------------------

    @Test
    @DisplayName("assignPermissionToRole_permissionNotFound: should throw ResourceNotFoundException")
    void assignPermissionToRole_permissionNotFound() {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permissionId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(sampleRole));
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rolePermissionService.assignPermissionToRole(request));

        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    }

    // ------------------------------------------------------------------
    // GET BY ID — Happy Path
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getMappingById_success: should return the mapping response")
    void getMappingById_success() {
        when(rolePermissionRepository.findById(mappingId)).thenReturn(Optional.of(sampleMapping));

        RolePermissionResponse response = rolePermissionService.getMappingById(mappingId);

        assertNotNull(response);
        assertEquals(mappingId, response.getId());
        assertEquals("Admin", response.getRoleName());
        assertEquals("VIEW_STUDENTS", response.getPermissionCode());
    }

    // ------------------------------------------------------------------
    // GET BY ID — Not Found
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getMappingById_notFound: should throw ResourceNotFoundException")
    void getMappingById_notFound() {
        when(rolePermissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rolePermissionService.getMappingById(UUID.randomUUID()));
    }

    // ------------------------------------------------------------------
    // GET ALL
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAllMappings_success: should return all active mappings")
    void getAllMappings_success() {
        RolePermission mapping2 = RolePermission.builder()
                .role(sampleRole)
                .permission(samplePermission)
                .build();
        mapping2.setId(UUID.randomUUID());

        when(rolePermissionRepository.findAllActiveWithDetails())
                .thenReturn(List.of(sampleMapping, mapping2));

        List<RolePermissionResponse> responses = rolePermissionService.getAllMappings();

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    // ------------------------------------------------------------------
    // GET PERMISSIONS BY ROLE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getPermissionsByRole_success: should return permissions for the given role")
    void getPermissionsByRole_success() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(sampleRole));
        when(rolePermissionRepository.findByRoleAndDeletedFalse(sampleRole))
                .thenReturn(List.of(sampleMapping));

        List<RolePermissionResponse> responses = rolePermissionService.getPermissionsByRole(roleId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("VIEW_STUDENTS", responses.get(0).getPermissionCode());
    }

    @Test
    @DisplayName("getPermissionsByRole_roleNotFound: should throw ResourceNotFoundException")
    void getPermissionsByRole_roleNotFound() {
        when(roleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rolePermissionService.getPermissionsByRole(UUID.randomUUID()));
    }

    // ------------------------------------------------------------------
    // GET ROLES BY PERMISSION
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getRolesByPermission_success: should return roles holding the given permission")
    void getRolesByPermission_success() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));
        when(rolePermissionRepository.findByPermissionAndDeletedFalse(samplePermission))
                .thenReturn(List.of(sampleMapping));

        List<RolePermissionResponse> responses = rolePermissionService.getRolesByPermission(permissionId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Admin", responses.get(0).getRoleName());
    }

    @Test
    @DisplayName("getRolesByPermission_permissionNotFound: should throw ResourceNotFoundException")
    void getRolesByPermission_permissionNotFound() {
        when(permissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rolePermissionService.getRolesByPermission(UUID.randomUUID()));
    }

    // ------------------------------------------------------------------
    // DELETE (soft)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("removePermissionFromRole_success: should soft-delete the mapping")
    void removePermissionFromRole_success() {
        when(rolePermissionRepository.findById(mappingId)).thenReturn(Optional.of(sampleMapping));

        rolePermissionService.removePermissionFromRole(mappingId);

        assertTrue(sampleMapping.isDeleted());
        assertNotNull(sampleMapping.getDeletedAt());
        assertEquals("system", sampleMapping.getDeletedBy());
        verify(rolePermissionRepository).save(sampleMapping);
    }

    @Test
    @DisplayName("removePermissionFromRole_notFound: should throw ResourceNotFoundException")
    void removePermissionFromRole_notFound() {
        when(rolePermissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rolePermissionService.removePermissionFromRole(UUID.randomUUID()));

        verify(rolePermissionRepository, never()).save(any(RolePermission.class));
    }
}
