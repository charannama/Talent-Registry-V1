package com.zencube.registry.chat.mapper;

import com.zencube.registry.chat.dto.response.ApplicationContextResponse;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.chat.dto.response.ChatMessageResponse;
import com.zencube.registry.chat.dto.response.ChatParticipantResponse;
import com.zencube.registry.chat.dto.response.ChatThreadResponse;
import com.zencube.registry.chat.dto.response.ParticipantSummaryResponse;
import com.zencube.registry.chat.dto.response.UserSummaryResponse;
import com.zencube.registry.chat.entity.ChatMessage;
import com.zencube.registry.chat.entity.ChatParticipant;
import com.zencube.registry.chat.entity.ChatThread;
import com.zencube.registry.common.enums.RoleType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMapper {

    public ChatThreadResponse toThreadResponse(ChatThread thread, List<ChatParticipant> participants, ChatMessage lastMessage, Integer unreadCount, ApplicationContextResponse applicationContext) {
        if (thread == null) {
            return null;
        }

        String lastMessagePreview = null;
        if (lastMessage != null && lastMessage.getContent() != null) {
            lastMessagePreview = lastMessage.getContent().length() > 50 
                    ? lastMessage.getContent().substring(0, 50) + "..." 
                    : lastMessage.getContent();
        }

        return ChatThreadResponse.builder()
                .id(thread.getId())
                .threadType(thread.getThreadType())
                .archived(thread.getIsArchived())
                .contextableType(thread.getContextableType())
                .contextableId(thread.getContextableId())
                .createdAt(thread.getCreatedAt())
                .createdBy(toUserSummaryResponse(thread.getCreator()))
                .participants(participants != null ? participants.stream().map(this::toParticipantSummaryResponse).collect(Collectors.toList()) : null)
                .unreadCount(unreadCount != null ? unreadCount : 0)
                .lastMessagePreview(lastMessagePreview)
                .lastMessageAt(lastMessage != null ? lastMessage.getSentAt() : null)
                .application(applicationContext)
                .build();
    }

    public ParticipantSummaryResponse toParticipantSummaryResponse(ChatParticipant participant) {
        if (participant == null) {
            return null;
        }

        User user = participant.getUser();
        return ParticipantSummaryResponse.builder()
                .id(participant.getId())
                .name(user != null ? user.getDisplayName() : null)
                .email(user != null ? user.getEmail() : null)
                .role(user != null ? getPrimaryRole(user) : null)
                .active(user != null ? user.isActive() : false)
                .joinedAt(participant.getJoinedAt())
                .build();
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getDisplayName())
                .email(user.getEmail())
                .role(getPrimaryRole(user))
                .build();
    }

    public ChatMessageResponse toMessageResponse(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return ChatMessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThread() != null ? message.getThread().getId() : null)
                .sender(toUserSummaryResponse(message.getSender()))
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .deleted(message.getDeletedAt() != null)
                .deletedAt(message.getDeletedAt())
                .build();
    }
    
    private RoleType getPrimaryRole(User user) {
        if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
            return user.getUserRoles().iterator().next().getRole().getRoleType();
        }
        return null;
    }
}

