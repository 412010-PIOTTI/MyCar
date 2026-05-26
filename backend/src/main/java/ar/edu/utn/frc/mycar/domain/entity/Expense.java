package ar.edu.utn.frc.mycar.domain.entity;

import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

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
    private ExpenseCategory category;

    /**
     * Subcategoría libre (ej: COMBUSTIBLE, SERVICIO, MULTA).
     * La categoría ADMINISTRATIVO es exclusiva del rol CONCESIONARIO
     * y se valida a nivel de servicio.
     */
    @Column(length = 50)
    private String subcategory;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 300)
    private String description;

    /** Kilometraje al momento del gasto (opcional) */
    @Column(name = "km_at_expense")
    private Integer kmAtExpense;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
