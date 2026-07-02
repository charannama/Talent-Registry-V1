package com.zencube.registry.calendar.entity;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import com.zencube.registry.calendar.enums.ParticipantType;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "calendar_participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarParticipant extends BaseEntity {



    @NotNull(message = "Event is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Email(message = "Must be a valid email address")
    @Column(name = "external_email")
    private String externalEmail;

    @NotNull(message = "Participant type is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false)
    private ParticipantType participantType;

    @NotNull(message = "Response status is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "response_status", nullable = false)
    private ParticipantResponseStatus responseStatus;



    @PrePersist
    @PreUpdate
    private void validateParticipant() {
        if (responseStatus == null) {
            responseStatus = ParticipantResponseStatus.PENDING;
        }
        
        if (participantType == ParticipantType.INTERNAL) {
            if (user == null) {
                throw new IllegalStateException("Internal participant requires a user");
            }
            if (externalEmail != null) {
                throw new IllegalStateException("Internal participant cannot have an external email");
            }
        } else if (participantType == ParticipantType.EXTERNAL) {
            if (externalEmail == null || externalEmail.trim().isEmpty()) {
                throw new IllegalStateException("External participant requires an external email");
            }
            if (user != null) {
                throw new IllegalStateException("External participant cannot have a user");
            }
        }
    }

    public void accept() {
        this.responseStatus = ParticipantResponseStatus.ACCEPTED;
    }

    public void decline() {
        this.responseStatus = ParticipantResponseStatus.DECLINED;
    }

    public void tentative() {
        this.responseStatus = ParticipantResponseStatus.TENTATIVE;
    }

    public void resetResponse() {
        this.responseStatus = ParticipantResponseStatus.PENDING;
    }

    public boolean isInternal() {
        return this.participantType == ParticipantType.INTERNAL;
    }

    public boolean isExternal() {
        return this.participantType == ParticipantType.EXTERNAL;
    }

    public String getParticipantDisplayName() {
        if (isInternal() && user != null) {
            // Assume user has an email or a name. Using email as standard display for now.
            return user.getEmail();
        }
        return externalEmail;
    }
}
