package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Expense;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.repository.ExpenseRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateExpenseRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
import ar.edu.utn.frc.mycar.web.exception.ExpenseNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    @Transactional
    public ExpenseResponse create(String ownerEmail, Long vehicleId, CreateExpenseRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(vehicleId, ownerEmail)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        if (request.getKmAtExpense() != null && request.getKmAtExpense() > vehicle.getCurrentKm()) {
            vehicle.setCurrentKm(request.getKmAtExpense());
        }

        User user = userService.getEntity(ownerEmail);
        Expense expense = Expense.builder()
                .vehicle(vehicle)
                .user(user)
                .category(request.getCategory())
                .subcategory(request.getSubcategory())
                .date(request.getDate())
                .amount(request.getAmount())
                .description(request.getDescription())
                .kmAtExpense(request.getKmAtExpense())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll(String ownerEmail, Long vehicleId) {
        if (!vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(vehicleId, ownerEmail)) {
            throw new VehicleNotFoundException(vehicleId);
        }
        return expenseRepository
                .findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(vehicleId, ownerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(String ownerEmail, Long vehicleId, Long expenseId) {
        if (!vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(vehicleId, ownerEmail)) {
            throw new VehicleNotFoundException(vehicleId);
        }
        return expenseRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(expenseId, vehicleId, ownerEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getVehicle().getId(),
                expense.getCategory(),
                expense.getSubcategory(),
                expense.getDate(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getKmAtExpense(),
                expense.getCreatedAt()
        );
    }
}
