package com.zencube.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.rolepermission.controller.RolePermissionController;
import com.zencube.registry.rolepermission.dto.CreateRolePermissionRequest;
import com.zencube.registry.rolepermission.dto.RolePermissionResponse;
import com.zencube.registry.rolepermission.service.RolePermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests for {@link RolePermissionController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer (no Spring context, no DB).
 * Security filters are disabled with {@code @AutoConfigureMockMvc(addFilters = false)}
 * so tests focus purely on HTTP semantics and JSON response shape.
 */
@WebMvcTest(RolePermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RolePermissionController Integration Tests")
class RolePermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @MockBean
    private RolePermissionService rolePermissionService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---- reusable fixture builder ----
    private RolePermissionResponse buildResponse(UUID mappingId, UUID roleId, UUID permId) {
        return RolePermissionResponse.builder()
                .id(mappingId)
                .roleId(roleId)
                .roleName("Admin")
                .permissionId(permId)
                .permissionName("View Students")
                .permissionCode("VIEW_STUDENTS")
                .createdAt(Instant.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/role-permissions  →  201 Created
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/role-permissions - 201 Created with valid payload")
    void assignPermission_returns201() throws Exception {
        UUID roleId  = UUID.randomUUID();
        UUID permId  = UUID.randomUUID();
        UUID mapId   = UUID.randomUUID();

        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, permId);
        RolePermissionResponse response    = buildResponse(mapId, roleId, permId);

        when(rolePermissionService.assignPermissionToRole(any(CreateRolePermissionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/role-permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.data.id").value(mapId.toString()))
                .andExpect(jsonPath("$.data.roleName").value("Admin"))
                .andExpect(jsonPath("$.data.permissionCode").value("VIEW_STUDENTS"));
    }

    @Test
    @DisplayName("POST /api/v1/role-permissions - 400 Bad Request when roleId is null")
    void assignPermission_returns400_whenRoleIdNull() throws Exception {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(null, UUID.randomUUID());

        mockMvc.perform(
                post("/api/v1/role-permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/role-permissions - 400 Bad Request when permissionId is null")
    void assignPermission_returns400_whenPermissionIdNull() throws Exception {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(UUID.randomUUID(), null);

        mockMvc.perform(
                post("/api/v1/role-permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/role-permissions - 404 when role not found")
    void assignPermission_returns404_whenRoleNotFound() throws Exception {
        UUID roleId = UUID.randomUUID();
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(roleId, UUID.randomUUID());

        when(rolePermissionService.assignPermissionToRole(any(CreateRolePermissionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Role", "id", roleId));

        mockMvc.perform(
                post("/api/v1/role-permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/role-permissions - 409 when mapping already exists")
    void assignPermission_returns409_whenDuplicate() throws Exception {
        CreateRolePermissionRequest request = new CreateRolePermissionRequest(
                UUID.randomUUID(), UUID.randomUUID());

        when(rolePermissionService.assignPermissionToRole(any(CreateRolePermissionRequest.class)))
                .thenThrow(new ConflictException("Permission 'VIEW_STUDENTS' is already assigned to role 'Admin'"));

        mockMvc.perform(
                post("/api/v1/role-permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions  →  200 OK
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/role-permissions - 200 OK with list of mappings")
    void getAllMappings_returns200() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(rolePermissionService.getAllMappings()).thenReturn(
                List.of(
                    buildResponse(id1, UUID.randomUUID(), UUID.randomUUID()),
                    buildResponse(id2, UUID.randomUUID(), UUID.randomUUID())
                )
        );

        mockMvc.perform(get("/api/v1/role-permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.data[1].id").value(id2.toString()))
                .andExpect(jsonPath("$.data[0].permissionCode").value("VIEW_STUDENTS"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions/{id}  →  200 OK / 404
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/role-permissions/{id} - 200 OK for existing mapping")
    void getMappingById_returns200() throws Exception {
        UUID mapId  = UUID.randomUUID();
        RolePermissionResponse response = buildResponse(mapId, UUID.randomUUID(), UUID.randomUUID());

        when(rolePermissionService.getMappingById(mapId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/role-permissions/{id}", mapId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mapId.toString()))
                .andExpect(jsonPath("$.data.roleName").value("Admin"));
    }

    @Test
    @DisplayName("GET /api/v1/role-permissions/{id} - 404 for unknown mapping")
    void getMappingById_returns404() throws Exception {
        UUID mapId = UUID.randomUUID();
        when(rolePermissionService.getMappingById(mapId))
                .thenThrow(new ResourceNotFoundException("RolePermission", "id", mapId));

        mockMvc.perform(get("/api/v1/role-permissions/{id}", mapId))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions/role/{roleId}  →  200 OK / 404
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/role-permissions/role/{roleId} - 200 OK with permissions list")
    void getPermissionsByRole_returns200() throws Exception {
        UUID roleId = UUID.randomUUID();
        UUID mapId  = UUID.randomUUID();

        when(rolePermissionService.getPermissionsByRole(roleId))
                .thenReturn(List.of(buildResponse(mapId, roleId, UUID.randomUUID())));

        mockMvc.perform(get("/api/v1/role-permissions/role/{roleId}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roleId").value(roleId.toString()))
                .andExpect(jsonPath("$.data[0].permissionCode").value("VIEW_STUDENTS"));
    }

    @Test
    @DisplayName("GET /api/v1/role-permissions/role/{roleId} - 404 when role not found")
    void getPermissionsByRole_returns404() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(rolePermissionService.getPermissionsByRole(roleId))
                .thenThrow(new ResourceNotFoundException("Role", "id", roleId));

        mockMvc.perform(get("/api/v1/role-permissions/role/{roleId}", roleId))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/role-permissions/permission/{permissionId}  →  200 OK / 404
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/role-permissions/permission/{permissionId} - 200 OK with roles list")
    void getRolesByPermission_returns200() throws Exception {
        UUID permId = UUID.randomUUID();
        UUID mapId  = UUID.randomUUID();

        when(rolePermissionService.getRolesByPermission(permId))
                .thenReturn(List.of(buildResponse(mapId, UUID.randomUUID(), permId)));

        mockMvc.perform(get("/api/v1/role-permissions/permission/{permissionId}", permId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permissionId").value(permId.toString()))
                .andExpect(jsonPath("$.data[0].roleName").value("Admin"));
    }

    @Test
    @DisplayName("GET /api/v1/role-permissions/permission/{permissionId} - 404 when permission not found")
    void getRolesByPermission_returns404() throws Exception {
        UUID permId = UUID.randomUUID();
        when(rolePermissionService.getRolesByPermission(permId))
                .thenThrow(new ResourceNotFoundException("Permission", "id", permId));

        mockMvc.perform(get("/api/v1/role-permissions/permission/{permissionId}", permId))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/role-permissions/{id}  →  204 / 404
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/role-permissions/{id} - 204 No Content on success")
    void removePermission_returns204() throws Exception {
        UUID mapId = UUID.randomUUID();
        doNothing().when(rolePermissionService).removePermissionFromRole(mapId);

        mockMvc.perform(delete("/api/v1/role-permissions/{id}", mapId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/role-permissions/{id} - 404 when mapping not found")
    void removePermission_returns404() throws Exception {
        UUID mapId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("RolePermission", "id", mapId))
                .when(rolePermissionService).removePermissionFromRole(mapId);

        mockMvc.perform(delete("/api/v1/role-permissions/{id}", mapId))
                .andExpect(status().isNotFound());
    }
}


