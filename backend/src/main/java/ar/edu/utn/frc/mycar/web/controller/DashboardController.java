package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.DashboardService;
import ar.edu.utn.frc.mycar.web.dto.response.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Main-screen status summary across all of the user's vehicles")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard summary",
            description = "Returns, per vehicle owned by the authenticated user: active alerts, key documents, "
                    + "last maintenance service, and current km. Also returns total expenses for the current "
                    + "month/year across all vehicles, and — for CONCESIONARIO users only — the day's USD/ARS "
                    + "exchange rate.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard returned.",
                    content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public DashboardResponse getDashboard(Authentication authentication) {
        return dashboardService.getDashboard(authentication.getName());
    }
}
