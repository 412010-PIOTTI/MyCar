package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body for POST /api/chat. */
@Schema(description = "AI chat reply")
public record ChatResponse(

        @Schema(description = "Assistant's reply")
        String reply,

        @Schema(description = "Whether the reply was grounded in the vehicle's manual, "
                + "or is general knowledge because no manual is available for that brand/model")
        boolean manualGrounded,

        @Schema(description = "Id of the vehicle the question was about", example = "1")
        Long vehicleId
) {}
