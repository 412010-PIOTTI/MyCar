package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Expense;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /** Used when purging a user's personal data for a vehicle whose ownership history must be kept. */
    void deleteByVehicleId(Long vehicleId);

    List<Expense> findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(Long vehicleId, String ownerEmail);

    List<Expense> findByVehicleIdAndVehicleOwnerEmailAndCategoryOrderByDateDescIdDesc(
            Long vehicleId, String ownerEmail, ExpenseCategory category);

    Optional<Expense> findByIdAndVehicleIdAndVehicleOwnerEmail(Long id, Long vehicleId, String ownerEmail);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.vehicle.id = :vehicleId
              AND e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
              AND MONTH(e.date) = :month
            """)
    BigDecimal sumByVehicleAndYearMonth(
            @Param("vehicleId") Long vehicleId,
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
            SELECT e.category AS category, COALESCE(SUM(e.amount), 0) AS total
            FROM Expense e
            WHERE e.vehicle.id = :vehicleId
              AND e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
              AND MONTH(e.date) = :month
            GROUP BY e.category
            """)
    List<CategoryTotal> sumByCategoryForYearMonth(
            @Param("vehicleId") Long vehicleId,
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
            SELECT MONTH(e.date) AS month, COALESCE(SUM(e.amount), 0) AS total
            FROM Expense e
            WHERE e.vehicle.id = :vehicleId
              AND e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
            GROUP BY MONTH(e.date)
            ORDER BY MONTH(e.date)
            """)
    List<MonthTotal> sumByMonthForYear(
            @Param("vehicleId") Long vehicleId,
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year);

    // ── All-vehicles queries (no vehicleId filter) ────────────────────────────

    List<Expense> findByVehicleOwnerEmailOrderByDateDescIdDesc(String ownerEmail);

    List<Expense> findByVehicleOwnerEmailAndCategoryOrderByDateDescIdDesc(
            String ownerEmail, ExpenseCategory category);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
              AND MONTH(e.date) = :month
            """)
    BigDecimal sumByOwnerAndYearMonth(
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
            SELECT e.category AS category, COALESCE(SUM(e.amount), 0) AS total
            FROM Expense e
            WHERE e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
              AND MONTH(e.date) = :month
            GROUP BY e.category
            """)
    List<CategoryTotal> sumByCategoryForOwnerAndYearMonth(
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year,
            @Param("month") int month);

    @Query("""
            SELECT MONTH(e.date) AS month, COALESCE(SUM(e.amount), 0) AS total
            FROM Expense e
            WHERE e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
            GROUP BY MONTH(e.date)
            ORDER BY MONTH(e.date)
            """)
    List<MonthTotal> sumByMonthForOwnerAndYear(
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.vehicle.owner.email = :ownerEmail
              AND YEAR(e.date) = :year
            """)
    BigDecimal sumByOwnerAndYear(
            @Param("ownerEmail") String ownerEmail,
            @Param("year") int year);

    interface CategoryTotal {
        ExpenseCategory getCategory();
        BigDecimal getTotal();
    }

    interface MonthTotal {
        int getMonth();
        BigDecimal getTotal();
    }
}
