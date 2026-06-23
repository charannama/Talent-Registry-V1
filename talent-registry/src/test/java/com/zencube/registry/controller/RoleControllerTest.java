package com.zencube.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.controller.RoleController;
import com.zencube.registry.auth.dto.role.CreateRoleRequest;
import com.zencube.registry.auth.dto.role.RoleResponse;
import com.zencube.registry.auth.dto.role.UpdateRoleRequest;
import com.zencube.registry.auth.service.interfaces.RoleService;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @MockBean
    private RoleService roleService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateRole() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("ADMIN");
        request.setRoleType(RoleType.ADMIN);

        RoleResponse response = RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .roleType(RoleType.ADMIN)
                .isSystem(false)
                .build();

        when(roleService.createRole(any(CreateRoleRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("ADMIN"));
    }

    @Test
    void shouldGetRole() throws Exception {
        UUID id = UUID.randomUUID();
        RoleResponse response = RoleResponse.builder()
                .id(id)
                .name("ADMIN")
                .roleType(RoleType.ADMIN)
                .isSystem(false)
                .build();

        when(roleService.getRole(id)).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/roles/{id}", id)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("ADMIN"));
    }

    @Test
    void shouldGetAllRoles() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        RoleResponse response1 = RoleResponse.builder()
                .id(id1)
                .name("ADMIN")
                .roleType(RoleType.ADMIN)
                .isSystem(false)
                .build();
        RoleResponse response2 = RoleResponse.builder()
                .id(id2)
                .name("STUDENT")
                .roleType(RoleType.STUDENT)
                .isSystem(false)
                .build();

        when(roleService.getAllRoles()).thenReturn(List.of(response1, response2));

        mockMvc.perform(
                get("/api/v1/roles")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.data[1].id").value(id2.toString()));
    }

    @Test
    void shouldUpdateRole() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("SUPER_ADMIN");
        request.setRoleType(RoleType.SUPER_ADMIN);

        RoleResponse response = RoleResponse.builder()
                .id(id)
                .name("SUPER_ADMIN")
                .roleType(RoleType.SUPER_ADMIN)
                .isSystem(false)
                .build();

        when(roleService.updateRole(eq(id), any(UpdateRoleRequest.class))).thenReturn(response);

        mockMvc.perform(
                put("/api/v1/roles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("SUPER_ADMIN"));
    }

    @Test
    void shouldDeleteRole() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(roleService).deleteRole(id);

        mockMvc.perform(
                delete("/api/v1/roles/{id}", id)
        )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenRoleDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.getRole(id)).thenThrow(new ResourceNotFoundException("Role", "id", id));

        mockMvc.perform(
                get("/api/v1/roles/{id}", id)
        )
                .andExpect(status().isNotFound());
    }
}


