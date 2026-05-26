package ar.edu.utn.frc.mycar.domain.entity;

import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.UrgencyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Ej: "Renovar ITV", "Cambio de aceite" */
    @Column(nullable = false, length = 100)
    private String title;

    /** DATE = alerta por fecha | KM = alerta por kilometraje */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 10)
    private AlertType alertType;

    /** Fecha límite (si alertType = DATE) */
    @Column(name = "alert_date")
    private LocalDate alertDate;

    /** Km límite (si alertType = KM) */
    @Column(name = "alert_km")
    private Integer alertKm;

    /** Días de anticipación para notificar (default: 30) */
    @Column(name = "advance_days")
    @Builder.Default
    private Integer advanceDays = 30;

    /**
     * Nivel de urgencia calculado al evaluar la alerta.
     * INFORMATIVA: 30+ días o 1000+ km.
     * ADVERTENCIA: 1–30 días o hasta 1000 km.
     * URGENTE: vencida o km superado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level", nullable = false, length = 15)
    @Builder.Default
    private UrgencyLevel urgencyLevel = UrgencyLevel.INFORMATIVA;

    /** Si ya se envió la notificación por email (SendGrid) */
    @Column(nullable = false)
    @Builder.Default
    private boolean notified = false;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
