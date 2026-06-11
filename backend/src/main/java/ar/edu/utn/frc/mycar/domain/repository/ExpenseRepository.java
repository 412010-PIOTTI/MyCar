package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(Long vehicleId, String ownerEmail);

    Optional<Expense> findByIdAndVehicleIdAndVehicleOwnerEmail(Long id, Long vehicleId, String ownerEmail);
}
