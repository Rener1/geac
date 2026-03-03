package br.com.geac.backend.api.controller;

import org.springframework.web.bind.annotation.*;
import br.com.geac.backend.aplication.dtos.request.EventRequestDTO;
import br.com.geac.backend.aplication.dtos.reponse.EventResponseDTO;
import br.com.geac.backend.aplication.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping(   "/create")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody @Valid EventRequestDTO dto) {
        EventResponseDTO response = eventService.createEvent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable UUID id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}
