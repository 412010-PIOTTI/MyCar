package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/chat. */
@Schema(description = "Payload to ask the AI chat a question about a vehicle")
@Getter
@Setter
@NoArgsConstructor
public class ChatRequest {

    @Schema(description = "Id of the vehicle the question is about", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El vehículo es obligatorio")
    private Long vehicleId;

    @Schema(description = "User's question", example = "¿Cada cuánto debo cambiar el aceite?",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 2000, message = "El mensaje no puede superar los 2000 caracteres")
    private String message;
}
