package ar.edu.utn.frc.mycar.domain.entity;

import ar.edu.utn.frc.mycar.domain.enums.MaintenanceSystem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceSystem system;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, name = "km_at_maintenance")
    private Integer kmAtMaintenance;

    @Column(length = 100)
    private String workshop;

    @Column(length = 300)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "next_service_km")
    private Integer nextServiceKm;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    /** Optional link to an Expense record for the same service (e.g. workshop invoice). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
