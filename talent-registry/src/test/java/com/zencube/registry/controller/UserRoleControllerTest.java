package com.zencube.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.userrole.controller.UserRoleController;
import com.zencube.registry.userrole.dto.CreateUserRoleRequest;
import com.zencube.registry.userrole.dto.UserRoleResponse;
import com.zencube.registry.userrole.service.UserRoleService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserRoleController Integration Tests")
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @MockBean
    private UserRoleService userRoleService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/user-roles - 201 Created")
    void assignRoleToUser_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CreateUserRoleRequest request = new CreateUserRoleRequest(userId, roleId);

        UserRoleResponse response = UserRoleResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .roleId(roleId)
                .roleName("ADMIN")
                .build();

        when(userRoleService.assignRoleToUser(any(CreateUserRoleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/user-roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/user-roles - 409 Conflict")
    void assignRoleToUser_returns409_onDuplicate() throws Exception {
        CreateUserRoleRequest request = new CreateUserRoleRequest(UUID.randomUUID(), UUID.randomUUID());

        when(userRoleService.assignRoleToUser(any(CreateUserRoleRequest.class)))
                .thenThrow(new ConflictException("Role already assigned"));

        mockMvc.perform(post("/api/v1/user-roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/user-roles/user/{userId} - 200 OK")
    void getRolesByUser_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userRoleService.getRolesByUser(userId)).thenReturn(List.of(
                UserRoleResponse.builder().roleName("ADMIN").build()
        ));

        mockMvc.perform(get("/api/v1/user-roles/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("DELETE /api/v1/user-roles/{id} - 204 No Content")
    void removeRoleFromUser_returns204() throws Exception {
        UUID mappingId = UUID.randomUUID();
        doNothing().when(userRoleService).removeRoleFromUser(mappingId);

        mockMvc.perform(delete("/api/v1/user-roles/{id}", mappingId))
                .andExpect(status().isNoContent());
    }
}


