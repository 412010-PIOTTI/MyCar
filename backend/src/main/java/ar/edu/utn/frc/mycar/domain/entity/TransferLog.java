package ar.edu.utn.frc.mycar.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro permanente e inmutable de una transferencia de titularidad ya
 * confirmada. A diferencia de {@link TransferToken}, nunca se invalida ni se
 * borra — es el historial de auditoría del vehículo.
 */
@Entity
@Table(name = "transfer_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_owner_id", nullable = false)
    private User fromOwner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_owner_id", nullable = false)
    private User toOwner;

    /** Token que se usó para confirmar esta transferencia. */
    @Column(nullable = false)
    private String token;

    @CreationTimestamp
    @Column(name = "transferred_at", nullable = false, updatable = false)
    private LocalDateTime transferredAt;
}
