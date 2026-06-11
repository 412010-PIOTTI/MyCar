package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Expense;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseStatus;
import ar.edu.utn.frc.mycar.domain.repository.ExpenseRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateExpenseRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseSummaryResponse;
import ar.edu.utn.frc.mycar.web.dto.response.MonthlyTotalResponse;
import ar.edu.utn.frc.mycar.web.exception.ExpenseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final VehicleService vehicleService;
    private final UserService userService;

    @Transactional
    public ExpenseResponse create(String ownerEmail, Long vehicleId, CreateExpenseRequest request) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);

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
                .expiryDate(request.getExpiryDate())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll(String ownerEmail, Long vehicleId, ExpenseCategory category) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        List<Expense> expenses = (category == null)
                ? expenseRepository.findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(vehicleId, ownerEmail)
                : expenseRepository.findByVehicleIdAndVehicleOwnerEmailAndCategoryOrderByDateDescIdDesc(vehicleId, ownerEmail, category);
        return expenses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(String ownerEmail, Long vehicleId, Long expenseId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        return expenseRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(expenseId, vehicleId, ownerEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(String ownerEmail, Long vehicleId, int year, int month) {
        vehicleService.getEntity(vehicleId, ownerEmail);

        BigDecimal current = expenseRepository.sumByVehicleAndYearMonth(vehicleId, ownerEmail, year, month);
        current = current != null ? current : BigDecimal.ZERO;

        int prevYear  = month == 1 ? year - 1 : year;
        int prevMonth = month == 1 ? 12 : month - 1;
        BigDecimal previous = expenseRepository.sumByVehicleAndYearMonth(vehicleId, ownerEmail, prevYear, prevMonth);
        previous = previous != null ? previous : BigDecimal.ZERO;

        Double percentageChange = null;
        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            percentageChange = current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        Map<ExpenseCategory, BigDecimal> byCategory = expenseRepository
                .sumByCategoryForYearMonth(vehicleId, ownerEmail, year, month)
                .stream()
                .collect(Collectors.toMap(
                        ExpenseRepository.CategoryTotal::category,
                        ExpenseRepository.CategoryTotal::total));

        return new ExpenseSummaryResponse(current, previous, percentageChange, byCategory);
    }

    @Transactional(readOnly = true)
    public List<MonthlyTotalResponse> getMonthlyTotals(String ownerEmail, Long vehicleId, int year) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        return expenseRepository.sumByMonthForYear(vehicleId, ownerEmail, year).stream()
                .map(r -> new MonthlyTotalResponse(r.month(), r.total()))
                .toList();
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
                expense.getExpiryDate(),
                computeStatus(expense.getExpiryDate()),
                expense.getCreatedAt()
        );
    }

    private ExpenseStatus computeStatus(LocalDate expiryDate) {
        if (expiryDate == null) return null;
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        if (daysLeft < 0) return ExpenseStatus.VENCIDO;
        if (daysLeft <= 30) return ExpenseStatus.POR_VENCER;
        return ExpenseStatus.VIGENTE;
    }
}
