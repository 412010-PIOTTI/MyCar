package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Overall vehicle system health derived from maintenance history")
public record MaintenanceHealthResponse(
        @Schema(description = "Overall health status: OPTIMO (all ≥70%), REGULAR (any <70%), CRITICO (any <30%)",
                example = "OPTIMO")
        String overallStatus,

        @Schema(description = "Per-system health breakdown")
        List<SystemHealthEntry> systems
) {}
