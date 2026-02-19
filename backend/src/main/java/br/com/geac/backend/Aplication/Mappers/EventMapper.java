package br.com.geac.backend.Aplication.Mappers;

import br.com.geac.backend.Aplication.DTOs.Reponse.EventResponseDTO;
import br.com.geac.backend.Aplication.DTOs.Reponse.LocationResponseDTO;
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

        LocationResponseDTO locationDTO = null;
        if (event.getLocation() != null) {
            locationDTO = new LocationResponseDTO(
                    event.getLocation().getId(),
                    event.getLocation().getName(),
                    event.getLocation().getStreet(),
                    event.getLocation().getNumber(),
                    event.getLocation().getNeighborhood(),
                    event.getLocation().getCity(),
                    event.getLocation().getState(),
                    event.getLocation().getZipCode(),
                    event.getLocation().getReferencePoint(),
                    event.getLocation().getCapacity()
            );
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

                event.getCategory() != null ? event.getCategory().getId() : null,
                event.getCategory() != null ? event.getCategory().getName() : null,

                locationDTO,

                event.getOrganizer() != null ? event.getOrganizer().getName() : null,
                event.getOrganizer() != null ? event.getOrganizer().getEmail() : null,

                event.getRequirement() != null ? event.getRequirement().getId() : null,
                event.getRequirement() != null ? event.getRequirement().getDescription() : null
        );
    }

    public List<EventResponseDTO> toDTOList(List<Event> events) {
        return events.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}