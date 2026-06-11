package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.ExpenseService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateExpenseRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
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
            description = "Registers a new expense for the given vehicle. If kmAtExpense is provided and greater than the vehicle's current odometer, the vehicle's currentKm is updated automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense registered successfully.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found or does not belong to the user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(Authentication authentication,
                                  @PathVariable Long vehicleId,
                                  @RequestBody @Valid CreateExpenseRequest request) {
        return expenseService.create(authentication.getName(), vehicleId, request);
    }

    @Operation(summary = "List expenses", description = "Returns all expenses for the given vehicle, ordered by date descending.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense list returned successfully.",
                    content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found or does not belong to the user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<ExpenseResponse> getAll(Authentication authentication, @PathVariable Long vehicleId) {
        return expenseService.getAll(authentication.getName(), vehicleId);
    }

    @Operation(summary = "Get an expense by id", description = "Returns a single expense by id, only if it belongs to the given vehicle owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense found and returned.",
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
}
