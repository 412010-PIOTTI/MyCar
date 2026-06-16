package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.ExpenseService;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseSummaryResponse;
import ar.edu.utn.frc.mycar.web.dto.response.MonthlyTotalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Aggregate expense endpoints — across all vehicles owned by the authenticated user. */
@Tag(name = "Expenses", description = "Aggregate expense endpoints (all vehicles)")
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class GlobalExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "List all expenses", description = "Returns all expenses across every vehicle owned by the user, ordered by date descending. Optionally filter by category.")
    @GetMapping
    public List<ExpenseResponse> getAll(Authentication authentication,
                                        @RequestParam(required = false) ExpenseCategory category) {
        return expenseService.getAllForOwner(authentication.getName(), category);
    }

    @Operation(summary = "Monthly summary (all vehicles)", description = "Returns the combined monthly summary across all vehicles for the given year/month.")
    @GetMapping("/summary")
    public ExpenseSummaryResponse getSummary(Authentication authentication,
                                             @RequestParam(required = false) Integer year,
                                             @RequestParam(required = false) Integer month) {
        LocalDate today = LocalDate.now();
        return expenseService.getSummaryForOwner(
                authentication.getName(),
                year  != null ? year  : today.getYear(),
                month != null ? month : today.getMonthValue());
    }

    @Operation(summary = "Monthly totals for chart (all vehicles)", description = "Returns combined monthly expense totals for all vehicles for the given year.")
    @GetMapping("/monthly-totals")
    public List<MonthlyTotalResponse> getMonthlyTotals(Authentication authentication,
                                                        @RequestParam(required = false) Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        return expenseService.getMonthlyTotalsForOwner(authentication.getName(), resolvedYear);
    }
}
