package com.zencube.registry.chat.controller;

import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import com.zencube.registry.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat", description = "Endpoints for Chat Module")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/threads")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create a new chat thread", description = "Creates a new chat thread. Requires HR or Admin role. Student <-> Enterprise direct chats are blocked.")
    @ApiResponse(responseCode = "201", description = "Thread successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "403", description = "Access denied or direct communication between student and enterprise blocked")
    @ApiResponse(responseCode = "409", description = "Conflict")
    public ChatThreadResponse createThread(
            @Valid @RequestBody CreateThreadRequest request
    ) {
        return chatService.createThread(request);
    }

    @GetMapping("/threads")
    @Operation(summary = "List threads for current user", description = "Returns a paginated list of chat threads the current user is a participant of.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved threads")
    public Page<ChatThreadResponse> listThreads(
            @Parameter(description = "Pagination and sorting information") Pageable pageable
    ) {
        return chatService.listThreads(pageable);
    }

    @GetMapping("/threads/{threadId}/messages")
    @Operation(summary = "Get messages for a thread", description = "Returns paginated messages for a given thread. User must be a participant.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    @ApiResponse(responseCode = "403", description = "User is not a participant")
    @ApiResponse(responseCode = "404", description = "Thread not found")
    public Page<ChatMessageResponse> getMessages(
            @Parameter(description = "UUID of the thread") @PathVariable UUID threadId,
            @Parameter(description = "Pagination and sorting information") Pageable pageable
    ) {
        return chatService.getMessages(threadId, pageable);
    }

    @PostMapping("/threads/{threadId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a message", description = "Sends a message to a given thread. User must be a participant.")
    @ApiResponse(responseCode = "201", description = "Message successfully sent")
    @ApiResponse(responseCode = "400", description = "Invalid message content")
    @ApiResponse(responseCode = "403", description = "User is not a participant")
    @ApiResponse(responseCode = "404", description = "Thread not found")
    public ChatMessageResponse sendMessage(
            @Parameter(description = "UUID of the thread") @PathVariable UUID threadId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return chatService.sendMessage(threadId, request);
    }

    @PostMapping("/threads/{threadId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark thread as read", description = "Marks all messages in the thread as read by updating the user's lastReadAt timestamp.")
    @ApiResponse(responseCode = "204", description = "Successfully marked as read")
    @ApiResponse(responseCode = "403", description = "User is not a participant")
    @ApiResponse(responseCode = "404", description = "Thread not found")
    public void markThreadAsRead(
            @Parameter(description = "UUID of the thread") @PathVariable UUID threadId
    ) {
        chatService.markThreadAsRead(threadId);
    }

    @PostMapping("/application/{applicationId}/thread")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an application thread", description = "Creates a new chat thread for an application context.")
    @ApiResponse(responseCode = "201", description = "Application thread successfully created")
    @ApiResponse(responseCode = "400", description = "Missing HR handler or valid participants")
    @ApiResponse(responseCode = "403", description = "Forbidden for non-HR")
    @ApiResponse(responseCode = "404", description = "Application not found")
    public ChatThreadResponse createApplicationThread(
            @Parameter(description = "UUID of the application") @PathVariable UUID applicationId
    ) {
        return chatService.createApplicationThread(applicationId);
    }
}
