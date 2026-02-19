package br.com.geac.backend.Aplication.DTOs.Reponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDTO(
    UUID id,
    String title,
    String description,
    String onlineLink,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer workloadHours,
    Integer maxCapacity,
    String status,
    LocalDateTime createdAt,

    Integer categoryId,
    String categoryName,

    LocationResponseDTO location,

    String organizerName,
    String organizerEmail,

    Integer requirementId,
    String requirementDescription
) {}
