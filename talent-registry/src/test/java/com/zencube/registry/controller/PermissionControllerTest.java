package com.zencube.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.controller.PermissionController;
import com.zencube.registry.auth.dto.permission.CreatePermissionRequest;
import com.zencube.registry.auth.dto.permission.PermissionResponse;
import com.zencube.registry.auth.dto.permission.UpdatePermissionRequest;
import com.zencube.registry.auth.service.interfaces.PermissionService;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
 * Controller-layer tests for {@link PermissionController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer (no Spring context, no DB).
 * Security filters are disabled with {@code @AutoConfigureMockMvc(addFilters = false)}
 * so tests focus purely on HTTP semantics and response structure.
 */
@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PermissionController Integration Tests")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @MockBean
    private PermissionService permissionService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------------------------------------------------------
    // POST /api/v1/permissions  →  201 Created
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/permissions - 201 Created with valid payload")
    void createPermission_returns201() throws Exception {
        CreatePermissionRequest request = CreatePermissionRequest.builder()
                .name("View Students")
                .code("VIEW_STUDENTS")
                .description("Can view student profiles")
                .build();

        PermissionResponse response = PermissionResponse.builder()
                .id(UUID.randomUUID())
                .name("View Students")
                .code("VIEW_STUDENTS")
                .description("Can view student profiles")
                .build();

        when(permissionService.createPermission(any(CreatePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.data.name").value("View Students"))
                .andExpect(jsonPath("$.data.code").value("VIEW_STUDENTS"))
                .andExpect(jsonPath("$.data.description").value("Can view student profiles"));
    }

    @Test
    @DisplayName("POST /api/v1/permissions - 400 Bad Request when name is blank")
    void createPermission_returns400_whenNameBlank() throws Exception {
        CreatePermissionRequest request = CreatePermissionRequest.builder()
                .name("")           // invalid: blank
                .code("VIEW_STUDENTS")
                .build();

        mockMvc.perform(
                post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/permissions - 400 Bad Request when code is blank")
    void createPermission_returns400_whenCodeBlank() throws Exception {
        CreatePermissionRequest request = CreatePermissionRequest.builder()
                .name("View Students")
                .code("")            // invalid: blank
                .build();

        mockMvc.perform(
                post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // GET /api/v1/permissions  →  200 OK
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/permissions - 200 OK with list of permissions")
    void getAllPermissions_returns200() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        PermissionResponse r1 = PermissionResponse.builder()
                .id(id1).name("View Students").code("VIEW_STUDENTS").build();
        PermissionResponse r2 = PermissionResponse.builder()
                .id(id2).name("Create Interview").code("CREATE_INTERVIEW").build();

        when(permissionService.getAllPermissions()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.data[0].code").value("VIEW_STUDENTS"))
                .andExpect(jsonPath("$.data[1].id").value(id2.toString()))
                .andExpect(jsonPath("$.data[1].code").value("CREATE_INTERVIEW"));
    }

    @Test
    @DisplayName("GET /api/v1/permissions - 200 OK with empty list when no permissions exist")
    void getAllPermissions_returns200_emptyList() throws Exception {
        when(permissionService.getAllPermissions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ------------------------------------------------------------------
    // GET /api/v1/permissions/{id}  →  200 OK / 404 Not Found
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/permissions/{id} - 200 OK for existing permission")
    void getPermission_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        PermissionResponse response = PermissionResponse.builder()
                .id(id)
                .name("View Students")
                .code("VIEW_STUDENTS")
                .build();

        when(permissionService.getPermission(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/permissions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("View Students"))
                .andExpect(jsonPath("$.data.code").value("VIEW_STUDENTS"));
    }

    @Test
    @DisplayName("GET /api/v1/permissions/{id} - 404 Not Found for unknown ID")
    void getPermission_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(permissionService.getPermission(id))
                .thenThrow(new ResourceNotFoundException("Permission", "id", id));

        mockMvc.perform(get("/api/v1/permissions/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // PUT /api/v1/permissions/{id}  →  200 OK / 404 Not Found
    // ------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} - 200 OK with updated permission")
    void updatePermission_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePermissionRequest request = UpdatePermissionRequest.builder()
                .name("View All Students")
                .description("Updated description")
                .build();

        PermissionResponse response = PermissionResponse.builder()
                .id(id)
                .name("View All Students")
                .code("VIEW_STUDENTS")           // code unchanged
                .description("Updated description")
                .build();

        when(permissionService.updatePermission(eq(id), any(UpdatePermissionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/v1/permissions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("View All Students"))
                .andExpect(jsonPath("$.data.code").value("VIEW_STUDENTS"))
                .andExpect(jsonPath("$.data.description").value("Updated description"));
    }

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} - 404 Not Found when permission absent")
    void updatePermission_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePermissionRequest request = UpdatePermissionRequest.builder()
                .name("View All Students")
                .build();

        when(permissionService.updatePermission(eq(id), any(UpdatePermissionRequest.class)))
                .thenThrow(new ResourceNotFoundException("Permission", "id", id));

        mockMvc.perform(
                put("/api/v1/permissions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} - 400 Bad Request when name is blank")
    void updatePermission_returns400_whenNameBlank() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePermissionRequest request = UpdatePermissionRequest.builder()
                .name("")           // invalid
                .build();

        mockMvc.perform(
                put("/api/v1/permissions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // DELETE /api/v1/permissions/{id}  →  204 No Content / 404 Not Found
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/permissions/{id} - 204 No Content on success")
    void deletePermission_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(permissionService).deletePermission(id);

        mockMvc.perform(delete("/api/v1/permissions/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/permissions/{id} - 404 Not Found when permission absent")
    void deletePermission_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Permission", "id", id))
                .when(permissionService).deletePermission(id);

        mockMvc.perform(delete("/api/v1/permissions/{id}", id))
                .andExpect(status().isNotFound());
    }
}


