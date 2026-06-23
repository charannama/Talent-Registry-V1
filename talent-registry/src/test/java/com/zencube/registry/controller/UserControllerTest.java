package com.zencube.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.user.controller.UserController;
import com.zencube.registry.user.dto.CreateUserRequest;
import com.zencube.registry.user.dto.UpdateUserRequest;
import com.zencube.registry.user.dto.UserAdminResponse;
import com.zencube.registry.user.service.UserService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @MockBean
    private UserService userService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserAdminResponse buildResponse(UUID id, String email) {
        return UserAdminResponse.builder()
                .id(id)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/users
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/users - 201 Created")
    void createUser_returns201() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        UUID id = UUID.randomUUID();
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(buildResponse(id, "test@example.com"));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/users - 400 Bad Request on invalid payload")
    void createUser_returns400_whenInvalid() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder().build(); // missing required fields

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users - 409 Conflict when email exists")
    void createUser_returns409_whenDuplicate() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("dup@example.com")
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new ConflictException("User", "email", "dup@example.com"));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/users
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/users - 200 OK")
    void getAllUsers_returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                buildResponse(UUID.randomUUID(), "user1@example.com"),
                buildResponse(UUID.randomUUID(), "user2@example.com")
        ));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/users/{id} - 200 OK")
    void getUser_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUser(id)).thenReturn(buildResponse(id, "test@example.com"));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - 404 Not Found")
    void getUser_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUser(id)).thenThrow(new ResourceNotFoundException("User", "id", id));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/users/{id} - 200 OK")
    void updateUser_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserRequest request = UpdateUserRequest.builder().firstName("NewName").build();

        UserAdminResponse response = buildResponse(id, "test@example.com");
        response.setFirstName("NewName");

        when(userService.updateUser(eq(id), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("NewName"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - 204 No Content")
    void deleteUser_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());
    }
}


