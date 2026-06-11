package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.ExpenseService;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import ar.edu.utn.frc.mycar.web.dto.request.CreateExpenseRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseSummaryResponse;
import ar.edu.utn.frc.mycar.web.dto.response.MonthlyTotalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Endpoints for vehicle expense management. */
@Tag(name = "Expenses", description = "Register and list vehicle expenses")
@RestController
@RequestMapping("/api/vehicles/{vehicleId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "Register an expense",
            description = "Registers a new expense. If kmAtExpense is greater than currentKm, the vehicle odometer is updated automatically. If expiryDate is provided, the expense status (VIGENTE/POR_VENCER/VENCIDO) is computed on read."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense registered successfully.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(Authentication authentication,
                                  @PathVariable Long vehicleId,
                                  @RequestBody @Valid CreateExpenseRequest request) {
        return expenseService.create(authentication.getName(), vehicleId, request);
    }

    @Operation(
            summary = "List expenses",
            description = "Returns all expenses for the vehicle ordered by date descending. Optionally filter by category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned successfully.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<ExpenseResponse> getAll(Authentication authentication,
                                        @PathVariable Long vehicleId,
                                        @RequestParam(required = false) ExpenseCategory category) {
        return expenseService.getAll(authentication.getName(), vehicleId, category);
    }

    @Operation(summary = "Get an expense by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense found.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Expense or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{expenseId}")
    public ExpenseResponse getById(Authentication authentication,
                                   @PathVariable Long vehicleId,
                                   @PathVariable Long expenseId) {
        return expenseService.getById(authentication.getName(), vehicleId, expenseId);
    }

    @Operation(
            summary = "Monthly summary",
            description = "Returns the total expenses for a given year/month, the percentage change vs the previous month, and a breakdown by category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned.",
                    content = @Content(schema = @Schema(implementation = ExpenseSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/summary")
    public ExpenseSummaryResponse getSummary(Authentication authentication,
                                             @PathVariable Long vehicleId,
                                             @RequestParam(required = false) Integer year,
                                             @RequestParam(required = false) Integer month) {
        LocalDate today = LocalDate.now();
        return expenseService.getSummary(
                authentication.getName(), vehicleId,
                year != null ? year : today.getYear(),
                month != null ? month : today.getMonthValue());
    }

    @Operation(
            summary = "Monthly totals for chart",
            description = "Returns the total expenses per month for a given year. Used to render the monthly comparison bar chart."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly totals returned.",
                    content = @Content(schema = @Schema(implementation = MonthlyTotalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/monthly-totals")
    public List<MonthlyTotalResponse> getMonthlyTotals(Authentication authentication,
                                                       @PathVariable Long vehicleId,
                                                       @RequestParam(required = false) Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        return expenseService.getMonthlyTotals(authentication.getName(), vehicleId, resolvedYear);
    }
}
