package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.RegistrationActionResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RegistrationResponseDTO;
import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Notification;
import br.com.geac.backend.domain.entities.Registration;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.enums.RegistrationStatus;
import br.com.geac.backend.domain.exceptions.*;
import br.com.geac.backend.infrastucture.repositories.EventRepository;
import br.com.geac.backend.infrastucture.repositories.OrganizerMemberRepository;
import br.com.geac.backend.infrastucture.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final OrganizerMemberRepository organizerMemberRepository;
    private final CertificateService certificateService;

    @Transactional
    public void markAttendanceInBulk(UUID eventId, List<UUID> userIds, boolean attended) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento não encontrado com o ID: " + eventId));

        //agora usa o metodo que valida se o usuario e Admin ou membro da organizacao
        validateOrganizerAccess(event);

        registrationRepository.updateAttendanceInBulk(eventId, userIds, attended);

        //emissao de certificados
        if (attended) {
            certificateService.issueCertificatesForEvent(eventId);
        }
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsByEvent(UUID eventId) {

        // 1. Busca o evento para validar o organizador
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento não encontrado."));

       validateOrganizerAccess(event);

        // 3. Busca as inscrições e converte para DTO
        List<Registration> registrations = registrationRepository
                .findByEventIdAndStatusOrderByRegistrationDateAsc(eventId, RegistrationStatus.CONFIRMED);

        return mapToResponse(registrations);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getWaitingListByEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento não encontrado."));

        validateOrganizerAccess(event);

        List<Registration> waitingList = registrationRepository
                .findByEventIdAndStatusOrderByRegistrationDateAsc(eventId, RegistrationStatus.WAITING_LIST);

        return mapToResponse(waitingList);
    }
    public List<Registration> getUnotifiedRegistrationsById(UUID eventId) {
        return registrationRepository.findByEventIdAndNotified(eventId,false);
    }
    @Transactional
    public RegistrationActionResponseDTO registerToEvent(UUID eventId) {

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento não encontrado com o ID: " + eventId));

        if (registrationRepository.existsByUserIdAndEventId(loggedUser.getId(), eventId)) {
            throw new UserAlreadySubscribedInEvent("Você já está inscrito neste evento.");
        }

        if (organizerMemberRepository.existsByOrganizerIdAndUserId(event.getOrganizer().getId(), loggedUser.getId())) {
            throw new MemberOfPromoterOrgException("Você não pode se inscrever no evento que sua organização está promovendo.");
        }

        if((event.getStatus() != EventStatus.ACTIVE) && (event.getStatus() != EventStatus.UPCOMING)) {
            throw new EventNotAvailableException("O Evento que voce está tentando se inscrever ainda nao está disponivel ou já foi encerrado");
        }

        long confirmedRegistrations = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
        boolean hasAvailableSpots = confirmedRegistrations < event.getMaxCapacity();

        Registration registration = new Registration();
        registration.setUser(loggedUser);
        registration.setEvent(event);
        registration.setStatus(hasAvailableSpots ? RegistrationStatus.CONFIRMED : RegistrationStatus.WAITING_LIST);
        registrationRepository.save(registration);

        Notification notification = buildRegistrationNotification(loggedUser, event, registration.getStatus());
        notificationService.notify(notification);
        log.info("Registrado com sucesso {}", notification.getMessage());

        if (registration.getStatus() == RegistrationStatus.WAITING_LIST) {
            return new RegistrationActionResponseDTO(
                    registration.getStatus().name(),
                    "Evento lotado. Você foi adicionado(a) à lista de espera."
            );
        }

        return new RegistrationActionResponseDTO(
                registration.getStatus().name(),
                "Parabéns! Sua inscrição no evento foi realizada com sucesso."
        );
    }

    @Transactional
    public void cancelRegistration(UUID eventId) {
        // 1. Pega o usuário logado
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Busca a inscrição dele no evento
        Registration registration = registrationRepository.findByUserIdAndEventId(loggedUser.getId(), eventId)
                .orElseThrow(() -> new RegistrationNotFoundException("Você não possui uma inscrição ativa neste evento."));

        // 3. (Opcional) Regra de negócio extra: Não permitir cancelar se a presença já foi dada
        if (Boolean.TRUE.equals(registration.getAttended())) {
            throw new BadRequestException("Não é possível cancelar a inscrição pois sua presença já foi validada no evento.");
        }
        Event event = registration.getEvent();


        notificationService.notify(buildCancellationNotification(loggedUser, event, registration.getStatus()));

        registrationRepository.delete(registration);
        promoteNextWaitingRegistrationIfNeeded(event, registration.getStatus());
    }

    public void saveAll(List<Registration> registrations) {
        registrationRepository.saveAll(registrations);
    }

    private void validateOrganizerAccess(Event event) {
        User loggedUser = getLoggedUser();

        boolean isAdmin = loggedUser.getRole() == br.com.geac.backend.domain.enums.Role.ADMIN;
        boolean isMember = organizerMemberRepository.existsByOrganizerIdAndUserId(event.getOrganizer().getId(), loggedUser.getId());

        if (!isAdmin && !isMember) {
            throw new BadRequestException("Acesso negado: Você não é membro da organização responsável por este evento.");
        }
    }

    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private List<RegistrationResponseDTO> mapToResponse(List<Registration> registrations) {
        return registrations.stream()
                .map(reg -> new RegistrationResponseDTO(
                        reg.getUser().getId(),
                        reg.getUser().getName(),
                        reg.getUser().getEmail(),
                        reg.getAttended(),
                        reg.getStatus().name()
                ))
                .toList();
    }

    private Notification buildRegistrationNotification(User user, Event event, RegistrationStatus status) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setType("SUBSCRIBE");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        if (status == RegistrationStatus.WAITING_LIST) {
            notification.setTitle("Adicionado à Lista de Espera");
            notification.setMessage("O evento '" + event.getTitle() + "' está lotado. Você entrou na lista de espera.");
            return notification;
        }

        notification.setTitle("Inscrição Confirmada");
        notification.setMessage("Parabéns! Sua inscrição no evento '" + event.getTitle() + "' foi realizada com sucesso.");
        return notification;
    }

    private void promoteNextWaitingRegistrationIfNeeded(Event event, RegistrationStatus canceledStatus) {
        if (canceledStatus != RegistrationStatus.CONFIRMED) {
            return;
        }

        registrationRepository.findFirstByEventIdAndStatusOrderByRegistrationDateAsc(
                        event.getId(),
                        RegistrationStatus.WAITING_LIST
                )
                .ifPresent(waitingRegistration -> {
                    waitingRegistration.setStatus(RegistrationStatus.CONFIRMED);
                    registrationRepository.save(waitingRegistration);
                    notificationService.notify(buildPromotionNotification(waitingRegistration.getUser(), event));
                });
    }

    private Notification buildPromotionNotification(User user, Event event) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setTitle("Vaga Liberada");
        notification.setMessage("Uma vaga foi liberada no evento '" + event.getTitle() + "' e sua inscrição foi confirmada.");
        notification.setType("SUBSCRIBE");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private Notification buildCancellationNotification(User user, Event event, RegistrationStatus status) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setType("CANCEL");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        if (status == RegistrationStatus.WAITING_LIST) {
            notification.setTitle("Saída da Lista de Espera");
            notification.setMessage("Você saiu da lista de espera do evento '" + event.getTitle() + "'.");
            return notification;
        }

        notification.setTitle("Inscrição Cancelada");
        notification.setMessage("Sua inscrição no evento '" + event.getTitle() + "' foi cancelada com sucesso. Uma vaga foi liberada.");
        return notification;
    }
}
