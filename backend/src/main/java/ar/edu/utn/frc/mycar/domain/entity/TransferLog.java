package ar.edu.utn.frc.mycar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Historial inmutable de transferencias completadas.
 * Se crea al confirmar una transferencia; nunca se modifica ni elimina.
 * Al completarse, vehicle.owner_id se actualiza al toUser.
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
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    /** Token utilizado para esta transferencia (1 token → 1 log) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "token_id", nullable = false, unique = true)
    private TransferToken token;

    @Column(name = "transferred_at", nullable = false, updatable = false)
    private LocalDateTime transferredAt;

    @PrePersist
    protected void onPersist() {
        transferredAt = LocalDateTime.now();
    }
}
