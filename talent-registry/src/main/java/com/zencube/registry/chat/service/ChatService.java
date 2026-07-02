package com.zencube.registry.chat.service;

import com.zencube.registry.chat.dto.request.CreateThreadRequest;
import com.zencube.registry.chat.dto.request.SendMessageRequest;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatService {

    ChatThreadResponse createThread(CreateThreadRequest request);

    ChatMessageResponse sendMessage(UUID threadId, SendMessageRequest request);

    Page<ChatThreadResponse> listThreads(Pageable pageable);

    Page<ChatMessageResponse> getMessages(UUID threadId, Pageable pageable);

    ChatThreadResponse getThread(UUID threadId);

    void markThreadAsRead(UUID threadId);

    long getUnreadCount(UUID threadId);

    java.util.Map<UUID, Long> getUnreadCounts(java.util.Collection<UUID> threadIds);

    ChatThreadResponse createApplicationThread(UUID applicationId);

}
