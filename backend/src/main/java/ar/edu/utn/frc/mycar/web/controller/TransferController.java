package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.TransferService;
import ar.edu.utn.frc.mycar.web.dto.response.TransferConfirmResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferGenerateResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferHistoryItemResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferPreviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Transfers", description = "Generate and redeem QR-based vehicle ownership transfer codes")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @Operation(
            summary = "Generate a transfer QR code",
            description = "Creates a new 48h transfer token for the vehicle and returns it together with a ready-to-display QR code. Invalidates any previously pending token for the same vehicle. Only the current owner can call this."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Token generated.",
                    content = @Content(schema = @Schema(implementation = TransferGenerateResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{vehicleId}/transfer/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferGenerateResponse generate(Authentication authentication, @PathVariable Long vehicleId) {
        return transferService.generate(authentication.getName(), vehicleId);
    }

    @Operation(
            summary = "Get the vehicle's active transfer token",
            description = "Returns the currently pending, non-expired token for this vehicle, or null if none. Only the current owner can call this."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active token returned (may be null).",
                    content = @Content(schema = @Schema(implementation = TransferGenerateResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{vehicleId}/transfer/active")
    public TransferGenerateResponse getActiveToken(Authentication authentication, @PathVariable Long vehicleId) {
        return transferService.getActiveToken(authentication.getName(), vehicleId);
    }

    @Operation(
            summary = "Get the user's full transfer history",
            description = "Returns every transfer token the authenticated user has ever generated as seller, across all their vehicles — including vehicles they no longer own after a completed transfer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History returned (may be empty).",
                    content = @Content(schema = @Schema(implementation = TransferHistoryItemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/transfer/history")
    public List<TransferHistoryItemResponse> getHistory(Authentication authentication) {
        return transferService.getHistory(authentication.getName());
    }

    @Operation(
            summary = "Preview a vehicle by transfer token",
            description = "Public endpoint used by the buyer after scanning the QR: returns basic vehicle data and its maintenance history in read-only mode. Does not require authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preview returned.",
                    content = @Content(schema = @Schema(implementation = TransferPreviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Token does not exist.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "410", description = "Token expired or already used.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @GetMapping("/transfer/{token}/preview")
    public TransferPreviewResponse preview(@PathVariable String token) {
        return transferService.preview(token);
    }

    @Operation(
            summary = "Confirm a vehicle transfer",
            description = "Redeems the token and assigns ownership of the vehicle to the authenticated user. The token becomes single-use and cannot be reused. The buyer must be logged in and cannot be the current owner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed.",
                    content = @Content(schema = @Schema(implementation = TransferConfirmResponse.class))),
            @ApiResponse(responseCode = "400", description = "The authenticated user is already the owner.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Token does not exist.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "410", description = "Token expired or already used.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/transfer/{token}/confirm")
    public TransferConfirmResponse confirm(Authentication authentication, @PathVariable String token) {
        return transferService.confirm(authentication.getName(), token);
    }
}
