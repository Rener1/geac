package br.com.geac.backend.Domain.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // TODO: Implement Category and Location entities and relationships
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "online_link", length = 255)
    private String onlineLink;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "workload_hours", nullable = false)
    private Integer workloadHours;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
