package com.zencube.registry.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import com.zencube.registry.chat.enums.ThreadType;
import com.zencube.registry.chat.service.ChatService;
import com.zencube.registry.security.facade.AuthenticationFacade;
import com.zencube.registry.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to test web layer quickly
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private AuthenticationFacade authenticationFacade;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = "HR_STAFF")
    void createThread_success() throws Exception {
        CreateThreadRequest request = new CreateThreadRequest(ThreadType.HR_STUDENT, List.of(UUID.randomUUID()), "Test", null);
        ChatThreadResponse response = ChatThreadResponse.builder().id(UUID.randomUUID()).build();

        when(chatService.createThread(any(CreateThreadRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/threads")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "HR_STAFF")
    void listThreads_success() throws Exception {
        ChatThreadResponse thread = ChatThreadResponse.builder().id(UUID.randomUUID()).build();
        Page<ChatThreadResponse> page = new PageImpl<>(List.of(thread));

        when(chatService.listThreads(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/chat/threads")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(thread.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getThread_success() throws Exception {
        UUID threadId = UUID.randomUUID();
        ChatThreadResponse response = ChatThreadResponse.builder().id(threadId).build();

        when(chatService.getThread(threadId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/chat/threads/{threadId}", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(threadId.toString()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getMessages_success() throws Exception {
        UUID threadId = UUID.randomUUID();
        ChatMessageResponse message = ChatMessageResponse.builder().id(UUID.randomUUID()).content("Hi").build();
        Page<ChatMessageResponse> page = new PageImpl<>(List.of(message));

        when(chatService.getMessages(eq(threadId), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/chat/threads/{threadId}/messages", threadId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(message.getId().toString()))
                .andExpect(jsonPath("$.content[0].content").value("Hi"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void sendMessage_success() throws Exception {
        UUID threadId = UUID.randomUUID();
        SendMessageRequest request = new SendMessageRequest("Hello there!");
        ChatMessageResponse response = ChatMessageResponse.builder().id(UUID.randomUUID()).content("Hello there!").build();

        when(chatService.sendMessage(eq(threadId), any(SendMessageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/threads/{threadId}/messages", threadId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.content").value("Hello there!"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void markThreadAsRead_success() throws Exception {
        UUID threadId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/chat/threads/{threadId}/read", threadId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(chatService).markThreadAsRead(threadId);
    }

    @Test
    @WithMockUser(roles = "HR_STAFF")
    void createApplicationThread_success() throws Exception {
        UUID applicationId = UUID.randomUUID();
        ChatThreadResponse response = ChatThreadResponse.builder().id(UUID.randomUUID()).build();

        when(chatService.createApplicationThread(applicationId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/application/{applicationId}/thread", applicationId)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()));
    }
}

