package com.zencube.registry.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.auth.dto.EnterpriseRegisterRequest;
import com.zencube.registry.auth.dto.RegistrationResponse;
import com.zencube.registry.auth.service.AuthService;
import com.zencube.registry.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnterpriseRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.zencube.registry.session.service.SessionService sessionService;



    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.zencube.registry.auth.service.OAuth2TokenExchangeService oauth2ExchangeService;

    @MockBean
    private com.zencube.registry.passwordreset.service.PasswordResetService passwordResetService;

    @MockBean
    private com.zencube.registry.security.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private EnterpriseRegisterRequest validRequest;
    private RegistrationResponse successResponse;

    @BeforeEach
    void setUp() {
        validRequest = new EnterpriseRegisterRequest(
                "John",
                "Doe",
                "john.doe@enterprise.com",
                "Secure@123",
                "Enterprise Corp"
        );
        successResponse = new RegistrationResponse(true, "Registration successful. Please verify your email before logging in.");
    }

    @Test
    void registerEnterprise_Success() throws Exception {
        when(authService.registerEnterprise(any(EnterpriseRegisterRequest.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/auth/register/enterprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("Registration successful. Please verify your email before logging in."))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Registration successful. Please verify your email before logging in."));
    }

    @Test
    void registerEnterprise_ValidationFailure_WeakPassword() throws Exception {
        EnterpriseRegisterRequest invalidRequest = new EnterpriseRegisterRequest(
                "John",
                "Doe",
                "john.doe@enterprise.com",
                "weak",
                "Enterprise Corp"
        );

        mockMvc.perform(post("/api/v1/auth/register/enterprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerEnterprise_ValidationFailure_InvalidEmail() throws Exception {
        EnterpriseRegisterRequest invalidRequest = new EnterpriseRegisterRequest(
                "John",
                "Doe",
                "invalid-email",
                "Secure@123",
                "Enterprise Corp"
        );

        mockMvc.perform(post("/api/v1/auth/register/enterprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerEnterprise_ValidationFailure_EmptyCompanyName() throws Exception {
        EnterpriseRegisterRequest invalidRequest = new EnterpriseRegisterRequest(
                "John",
                "Doe",
                "john.doe@enterprise.com",
                "Secure@123",
                ""
        );

        mockMvc.perform(post("/api/v1/auth/register/enterprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerEnterprise_DuplicateEmail() throws Exception {
        when(authService.registerEnterprise(any(EnterpriseRegisterRequest.class)))
                .thenThrow(new ConflictException("An account with email 'john.doe@enterprise.com' already exists."));

        mockMvc.perform(post("/api/v1/auth/register/enterprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerEnterprise_DuplicateCompany() throws Exception {
        when(authService.registerEnterprise(any(EnterpriseRegisterRequest.class)))
                .thenThrow(new ConflictException("Company name 'Enterprise Corp' is already registered."));

        mockMvc.perform(post("/api/v1/auth/register/enterprise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict());
    }
}


