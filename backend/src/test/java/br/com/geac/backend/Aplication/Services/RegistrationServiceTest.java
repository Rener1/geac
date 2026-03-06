package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.RegistrationResponseDTO;
import br.com.geac.backend.domain.entities.*;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.*;
import br.com.geac.backend.infrastucture.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private NotificationService notificationService;
    @Mock private OrganizerMemberRepository organizerMemberRepository;
    @Mock private CertificateService certificateService;

    @InjectMocks
    private RegistrationService registrationService;

    private User student;
    private User admin;
    private Event event;
    private Organizer organizer;
    private Registration registration;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setEmail("aluno@ufape.br");
        student.setName("Aluno Teste");
        student.setRole(Role.STUDENT);

        admin = new User();
        admin.setEmail("admin@ufape.br");
        admin.setName("Admin Teste");
        admin.setRole(Role.ADMIN);

        organizer = new Organizer();
        organizer.setId(UUID.randomUUID());
        organizer.setName("Org Teste");

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Palestra sobre IA");
        event.setStatus(EventStatus.ACTIVE);
        event.setOrganizer(organizer);
        event.setMaxCapacity(100);
        event.setSpeakers(new HashSet<>());
        event.setTags(new HashSet<>());
        event.setRequirements(new HashSet<>());

        registration = new Registration();
        registration.setUser(student);
        registration.setEvent(event);
        registration.setStatus("CONFIRMED");
        registration.setAttended(false);
    }

    // ==================== REGISTER TO EVENT ====================

    @Test
    @DisplayName("Deve inscrever usuÃ¡rio no evento com sucesso")
    void registerToEvent_Success() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);
        when(eventRepository.save(any())).thenReturn(event);
        when(registrationRepository.save(any())).thenReturn(registration);

        assertThatCode(() -> registrationService.registerToEvent(event.getId()))
                .doesNotThrowAnyException();

        verify(registrationRepository).save(any(Registration.class));
        verify(notificationService).notify(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando evento nÃ£o encontrado ao inscrever")
    void registerToEvent_EventNotFound_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.registerToEvent(UUID.randomUUID()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando usuÃ¡rio jÃ¡ estÃ¡ inscrito")
    void registerToEvent_AlreadySubscribed_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(UserAlreadySubscribedInEvent.class)
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando usuÃ¡rio Ã© membro da organizaÃ§Ã£o promotora")
    void registerToEvent_MemberOfPromoterOrg_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(MemberOfPromoterOrgException.class);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando evento estÃ¡ lotado")
    void registerToEvent_MaxCapacity_ThrowsException() {
        setAuthentication(student);
        event.setMaxCapacity(10);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(EventMaxCapacityAchievedException.class);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando evento nÃ£o estÃ¡ disponÃ­vel")
    void registerToEvent_EventNotAvailable_ThrowsException() {
        setAuthentication(student);
        event.setStatus(EventStatus.COMPLETED);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(5L);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(EventNotAvailableException.class);
    }

    @Test
    @DisplayName("Deve inscrever com sucesso em evento UPCOMING")
    void registerToEvent_UpcomingEvent_Success() {
        setAuthentication(student);
        event.setStatus(EventStatus.UPCOMING);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(5L);
        when(eventRepository.save(any())).thenReturn(event);
        when(registrationRepository.save(any())).thenReturn(registration);

        assertThatCode(() -> registrationService.registerToEvent(event.getId()))
                .doesNotThrowAnyException();
    }

    // ==================== CANCEL REGISTRATION ====================

    @Test
    @DisplayName("Deve cancelar inscriÃ§Ã£o com sucesso")
    void cancelRegistration_Success() {
        setAuthentication(student);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.of(registration));

        assertThatCode(() -> registrationService.cancelRegistration(event.getId()))
                .doesNotThrowAnyException();

        verify(registrationRepository).delete(registration);
        verify(notificationService).notify(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando inscriÃ§Ã£o nÃ£o encontrada ao cancelar")
    void cancelRegistration_NotFound_ThrowsException() {
        setAuthentication(student);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.cancelRegistration(event.getId()))
                .isInstanceOf(RegistrationNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando presenÃ§a jÃ¡ foi validada")
    void cancelRegistration_AlreadyAttended_ThrowsException() {
        setAuthentication(student);
        registration.setAttended(true);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.cancelRegistration(event.getId()))
                .isInstanceOf(BadRequestException.class)
                .isInstanceOf(Exception.class);
    }

    // ==================== GET REGISTRATIONS BY EVENT ====================

    @Test
    @DisplayName("Deve retornar lista de inscriÃ§Ãµes por evento")
    void getRegistrationsByEvent_Success() {
        setAuthentication(admin);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventId(event.getId())).thenReturn(List.of(registration));

        List<RegistrationResponseDTO> result = registrationService.getRegistrationsByEvent(event.getId());

        assertThat(result).isNotNull().hasSize(1);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando evento nÃ£o encontrado ao buscar inscriÃ§Ãµes")
    void getRegistrationsByEvent_EventNotFound_ThrowsException() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.getRegistrationsByEvent(UUID.randomUUID()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nÃ£o hÃ¡ inscriÃ§Ãµes")
    void getRegistrationsByEvent_EmptyList() {
        setAuthentication(admin);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventId(event.getId())).thenReturn(List.of());

        List<RegistrationResponseDTO> result = registrationService.getRegistrationsByEvent(event.getId());

        assertThat(result).isNotNull().isEmpty();
    }

    // ==================== MARK ATTENDANCE ====================

    @Test
    @DisplayName("Deve marcar presenÃ§a em bulk com sucesso como admin")
    void markAttendanceInBulk_AdminSuccess() {
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        List<UUID> userIds = List.of(UUID.randomUUID());

        assertThatCode(() -> registrationService.markAttendanceInBulk(event.getId(), userIds, true))
                .doesNotThrowAnyException();

        verify(registrationRepository).updateAttendanceInBulk(event.getId(), userIds, true);
        verify(certificateService).issueCertificatesForEvent(event.getId());
    }

    @Test
    @DisplayName("NÃ£o deve emitir certificados quando attended Ã© false")
    void markAttendanceInBulk_NotAttended_NoCertificates() {
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        List<UUID> userIds = List.of(UUID.randomUUID());

        registrationService.markAttendanceInBulk(event.getId(), userIds, false);

        verify(certificateService, never()).issueCertificatesForEvent(any());
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando evento nÃ£o encontrado ao marcar presenÃ§a")
    void markAttendanceInBulk_EventNotFound_ThrowsException() {
        setAuthentication(admin);

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.markAttendanceInBulk(UUID.randomUUID(), List.of(), true))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o quando usuÃ¡rio nÃ£o Ã© membro da organizaÃ§Ã£o")
    void markAttendanceInBulk_NotMember_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> registrationService.markAttendanceInBulk(event.getId(), List.of(), true))
                .isInstanceOf(BadRequestException.class);
    }

    // ==================== SAVE ALL ====================

    @Test
    @DisplayName("Deve salvar lista de inscriÃ§Ãµes")
    void saveAll_Success() {
        List<Registration> registrations = List.of(registration);
        registrationService.saveAll(registrations);
        verify(registrationRepository).saveAll(registrations);
    }

    // ==================== GET UNNOTIFIED ====================

    @Test
    @DisplayName("Deve retornar inscriÃ§Ãµes nÃ£o notificadas")
    void getUnotifiedRegistrationsById_Success() {
        when(registrationRepository.findByEventIdAndNotified(event.getId(), false))
                .thenReturn(List.of(registration));

        List<Registration> result = registrationService.getUnotifiedRegistrationsById(event.getId());

        assertThat(result).hasSize(1);
    }

    // ==================== HELPERS ====================

    private void setAuthentication(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
