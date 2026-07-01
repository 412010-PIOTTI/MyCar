package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** The active transfer token for a vehicle, including its ready-to-display QR image. */
@Schema(description = "Active transfer token and QR code")
public record TransferGenerateResponse(

        @Schema(description = "Raw UUID token", example = "8f14e45f-ceea-4e94-b7f0-3d3a7b2f6d1a")
        String token,

        @Schema(description = "Expiration timestamp, 48h after generation")
        LocalDateTime expiresAt,

        @Schema(description = "URL encoded in the QR code, opened by the buyer's phone")
        String transferUrl,

        @Schema(description = "QR code as a base64 PNG data URI, ready for an <img src>")
        String qrCodeBase64
) {}
