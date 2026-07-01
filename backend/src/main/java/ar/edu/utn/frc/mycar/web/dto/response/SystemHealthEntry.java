package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Health score for a single vehicle system")
public record SystemHealthEntry(
        @Schema(description = "System name", example = "Motor y Transmisión")
        String name,

        @Schema(description = "Health percentage 0-100. Null when no maintenance record exists for this system.", nullable = true, example = "85")
        Integer healthPct,

        @Schema(description = "Optional recommendation when health is below 50%.", nullable = true,
                example = "Rotación necesaria en 2.000 km")
        String recommendation
) {}
