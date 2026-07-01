package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.DocumentType;

import java.time.LocalDate;

public record DocumentSummaryResponse(
        int total,
        int vigente,
        int porVencer,
        int vencido,
        int sinFecha,
        int compliancePercentage,
        NextExpiring nextExpiring
) {
    public record NextExpiring(
            DocumentType type,
            String label,
            LocalDate expiryDate,
            long daysLeft
    ) {}
}
