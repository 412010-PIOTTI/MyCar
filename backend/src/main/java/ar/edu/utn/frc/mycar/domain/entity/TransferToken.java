package ar.edu.utn.frc.mycar.domain.entity;

import ar.edu.utn.frc.mycar.domain.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Token de un solo uso para transferencia vehicular mediante QR.
 * Expira 48hs después de su creación.
 */
@Entity
@Table(name = "transfer_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    /** UUID único embebido en el QR */
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private TransferStatus status = TransferStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Vence 48hs después de la creación */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
