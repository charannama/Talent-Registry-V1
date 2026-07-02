package com.zencube.registry.audit;

import com.zencube.registry.journal.entity.Journal;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.journal.repository.JournalRepository;
import com.zencube.registry.journal.service.AuditServiceImpl;
import com.zencube.registry.journal.util.EntityComparator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private JournalRepository journalRepository;

    @Mock
    private EntityComparator entityComparator;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    void recordCreate_ShouldPersistJournal() {
        // Set up security context so getCurrentUserId() works
        UUID actorId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actorId.toString(), null)
        );

        auditService.recordCreate("TEST_ENTITY", 1L);

        ArgumentCaptor<Journal> journalCaptor = ArgumentCaptor.forClass(Journal.class);
        verify(journalRepository).save(journalCaptor.capture());

        Journal saved = journalCaptor.getValue();
        assertThat(saved.getAction()).isEqualTo(JournalAction.CREATE);
        assertThat(saved.getJournableType()).isEqualTo("TEST_ENTITY");
        assertThat(saved.getJournableId()).isEqualTo(1L);
        assertThat(saved.getUserId()).isEqualTo(actorId);
    }

    @Test
    void recordDelete_ShouldPersistJournal() {
        UUID actorId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actorId.toString(), null)
        );

        auditService.recordDelete("TEST_ENTITY", 99L);

        verify(journalRepository, times(1)).save(any(Journal.class));
    }
}
