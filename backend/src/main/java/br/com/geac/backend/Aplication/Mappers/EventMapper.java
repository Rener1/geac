package br.com.geac.backend.Aplication.Mappers;

import br.com.geac.backend.Aplication.DTOs.Reponse.EventResponseDTO;
import br.com.geac.backend.Domain.Entities.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public EventResponseDTO toDTO(Event event) {
        if (event == null) {
            return null;
        }

        return new EventResponseDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getOnlineLink(),
                event.getStartTime(),
                event.getEndTime(),
                event.getWorkloadHours(),
                event.getMaxCapacity(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getCategoryId(),
                event.getLocationId(),
                event.getOrganizer() != null ? event.getOrganizer().getName() : null,
                event.getOrganizer() != null ? event.getOrganizer().getEmail() : null
        );
    }

    public List<EventResponseDTO> toDTOList(List<Event> events) {
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}