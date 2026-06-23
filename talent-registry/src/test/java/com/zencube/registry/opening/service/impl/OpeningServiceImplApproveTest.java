package com.zencube.registry.opening.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.exception.OpeningNotPendingApprovalException;
import com.zencube.registry.opening.repository.OpeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpeningServiceImplApproveTest {

    @Mock
    private OpeningRepository openingRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private OpeningServiceImpl openingService;

    private UUID openingId;
    private User hrUser;
    private Opening opening;

    @BeforeEach
    void setUp() {
        openingId = UUID.randomUUID();

        hrUser = new User();
        hrUser.setId(UUID.randomUUID());

        opening = new Opening();
        opening.setId(openingId);
        opening.setStatus(OpeningStatus.PENDING_APPROVAL);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should successfully approve opening")
    void approveOpening_Success() {
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(hrUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));
        when(openingRepository.save(any(Opening.class))).thenAnswer(i -> i.getArgument(0));

        OpeningResponse response = openingService.approveOpening(openingId);

        assertThat(response.getStatus()).isEqualTo(OpeningStatus.LIVE);
        assertThat(opening.getApprovedBy()).isEqualTo(hrUser.getId());
        assertThat(opening.getApprovedAt()).isNotNull();
        assertThat(opening.getPublishedAt()).isNotNull();
        verify(openingRepository).save(opening);
    }

    @Test
    @DisplayName("Should throw exception if opening not found")
    void approveOpening_NotFound() {
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(hrUser, null));
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> openingService.approveOpening(openingId));
    }

    @Test
    @DisplayName("Should throw exception if opening is not PENDING_APPROVAL")
    void approveOpening_NotPendingApproval() {
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(hrUser, null));
        opening.setStatus(OpeningStatus.DRAFT);
        when(openingRepository.findByIdAndDeletedFalse(openingId)).thenReturn(Optional.of(opening));

        assertThrows(OpeningNotPendingApprovalException.class, () -> openingService.approveOpening(openingId));
    }
}
