package ar.edu.utn.frc.mycar.domain.entity;

import ar.edu.utn.frc.mycar.domain.enums.MaintenanceSystem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historial técnico de mantenimiento del vehículo.
 * Registra intervenciones mecánicas, independiente del registro financiero (Expense).
 * Este registro NUNCA se elimina físicamente — es el activo principal del sistema.
 */
@Entity
@Table(name = "maintenance_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceSystem system;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "km_at_service", nullable = false)
    private Integer kmAtService;

    @Column(length = 150)
    private String workshop;

    /** Costo de referencia. El gasto formal va en {@link Expense}. */
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(length = 500)
    private String description;

    @Column(name = "next_service_km")
    private Integer nextServiceKm;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    /**
     * Gasto financiero asociado (opcional).
     * Un service puede generar simultáneamente un MaintenanceLog (técnico)
     * y un Expense (financiero). No siempre van juntos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registered_by", nullable = false)
    private User registeredBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
