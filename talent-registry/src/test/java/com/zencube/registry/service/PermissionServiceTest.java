package com.zencube.registry.service;

import com.zencube.registry.auth.dto.permission.CreatePermissionRequest;
import com.zencube.registry.auth.dto.permission.PermissionResponse;
import com.zencube.registry.auth.dto.permission.UpdatePermissionRequest;
import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.mapper.PermissionMapper;
import com.zencube.registry.auth.repository.PermissionRepository;
import com.zencube.registry.auth.service.impl.PermissionServiceImpl;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
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
 * Unit tests for {@link PermissionServiceImpl}.
 *
 * <p>Uses Mockito to isolate the service from its JPA repository dependency.
 * The {@link PermissionMapper} is a real {@code @Spy} so mapping logic is
 * exercised without loading the Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionServiceImpl Unit Tests")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Spy
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private UUID permissionId;
    private Permission samplePermission;

    @BeforeEach
    void setUp() {
        permissionId = UUID.randomUUID();
        samplePermission = Permission.builder()
                .name("View Students")
                .code("VIEW_STUDENTS")
                .description("Can view student profiles")
                .build();
        samplePermission.setId(permissionId);
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createPermission_success: should persist and return a PermissionResponse")
    void createPermission_success() {
        CreatePermissionRequest request = CreatePermissionRequest.builder()
                .name("View Students")
                .code("VIEW_STUDENTS")
                .description("Can view student profiles")
                .build();

        when(permissionRepository.existsByCodeAndDeletedFalse("VIEW_STUDENTS")).thenReturn(false);
        when(permissionRepository.existsByNameAndDeletedFalse("View Students")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(samplePermission);

        PermissionResponse response = permissionService.createPermission(request);

        assertNotNull(response);
        assertEquals("View Students", response.getName());
        assertEquals("VIEW_STUDENTS", response.getCode());
        assertEquals("Can view student profiles", response.getDescription());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("createPermission_duplicateCode: should throw ConflictException when code already exists")
    void createPermission_duplicateCode() {
        CreatePermissionRequest request = CreatePermissionRequest.builder()
                .name("View Students")
                .code("VIEW_STUDENTS")
                .build();

        when(permissionRepository.existsByCodeAndDeletedFalse("VIEW_STUDENTS")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> permissionService.createPermission(request));

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("createPermission_duplicateName: should throw ConflictException when name already exists")
    void createPermission_duplicateName() {
        CreatePermissionRequest request = CreatePermissionRequest.builder()
                .name("View Students")
                .code("VIEW_STUDENTS_V2")
                .build();

        when(permissionRepository.existsByCodeAndDeletedFalse("VIEW_STUDENTS_V2")).thenReturn(false);
        when(permissionRepository.existsByNameAndDeletedFalse("View Students")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> permissionService.createPermission(request));

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    // ------------------------------------------------------------------
    // GET BY ID
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getPermission_success: should return PermissionResponse for existing permission")
    void getPermission_success() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));

        PermissionResponse response = permissionService.getPermission(permissionId);

        assertNotNull(response);
        assertEquals(permissionId, response.getId());
        assertEquals("VIEW_STUDENTS", response.getCode());
    }

    @Test
    @DisplayName("getPermission_notFound: should throw ResourceNotFoundException when ID is unknown")
    void getPermission_notFound() {
        when(permissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> permissionService.getPermission(UUID.randomUUID()));
    }

    @Test
    @DisplayName("getPermission_softDeleted: should throw ResourceNotFoundException when permission is soft-deleted")
    void getPermission_softDeleted() {
        samplePermission.softDelete("system");
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));

        assertThrows(ResourceNotFoundException.class,
                () -> permissionService.getPermission(permissionId));
    }

    // ------------------------------------------------------------------
    // GET ALL
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAllPermissions_success: should return list of all active permissions")
    void getAllPermissions_success() {
        Permission p1 = Permission.builder().name("View Students").code("VIEW_STUDENTS").build();
        p1.setId(UUID.randomUUID());
        Permission p2 = Permission.builder().name("Create Interview").code("CREATE_INTERVIEW").build();
        p2.setId(UUID.randomUUID());

        when(permissionRepository.findAllByDeletedFalse()).thenReturn(List.of(p1, p2));

        List<PermissionResponse> responses = permissionService.getAllPermissions();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("VIEW_STUDENTS", responses.get(0).getCode());
        assertEquals("CREATE_INTERVIEW", responses.get(1).getCode());
        verify(permissionRepository).findAllByDeletedFalse();
    }

    @Test
    @DisplayName("getAllPermissions_empty: should return empty list when no permissions exist")
    void getAllPermissions_empty() {
        when(permissionRepository.findAllByDeletedFalse()).thenReturn(List.of());

        List<PermissionResponse> responses = permissionService.getAllPermissions();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updatePermission_success: should update name and description, leaving code unchanged")
    void updatePermission_success() {
        UpdatePermissionRequest request = UpdatePermissionRequest.builder()
                .name("View All Students")
                .description("Updated description")
                .build();

        Permission updatedPermission = Permission.builder()
                .name("View All Students")
                .code("VIEW_STUDENTS")          // code unchanged
                .description("Updated description")
                .build();
        updatedPermission.setId(permissionId);

        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(updatedPermission);

        PermissionResponse response = permissionService.updatePermission(permissionId, request);

        assertNotNull(response);
        assertEquals("View All Students", response.getName());
        assertEquals("VIEW_STUDENTS", response.getCode());    // code must remain unchanged
        assertEquals("Updated description", response.getDescription());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("updatePermission_notFound: should throw ResourceNotFoundException when permission absent")
    void updatePermission_notFound() {
        when(permissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        UpdatePermissionRequest request = UpdatePermissionRequest.builder()
                .name("View All Students")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> permissionService.updatePermission(UUID.randomUUID(), request));

        verify(permissionRepository, never()).save(any(Permission.class));
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    @Test
    @DisplayName("deletePermission_success: should soft-delete by setting isDeleted, deletedAt, deletedBy")
    void deletePermission_success() {
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(samplePermission));

        permissionService.deletePermission(permissionId);

        assertTrue(samplePermission.isDeleted());
        assertNotNull(samplePermission.getDeletedAt());
        assertEquals("system", samplePermission.getDeletedBy());
        verify(permissionRepository).save(samplePermission);
    }

    @Test
    @DisplayName("deletePermission_notFound: should throw ResourceNotFoundException when ID is unknown")
    void deletePermission_notFound() {
        when(permissionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> permissionService.deletePermission(UUID.randomUUID()));

        verify(permissionRepository, never()).save(any(Permission.class));
    }
}
