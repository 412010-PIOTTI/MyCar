package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.ChatService;
import ar.edu.utn.frc.mycar.web.dto.request.ChatRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint for the AI maintenance chat (RAG over manufacturer manuals). */
@Tag(name = "Chat", description = "Ask the AI assistant about a vehicle's maintenance")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Answers a maintenance question about one of the authenticated user's vehicles, grounded in
     * that vehicle's manufacturer manual when one is available.
     */
    @Operation(
            summary = "Ask the AI chat about a vehicle",
            description = """
                    Sends a question about the given vehicle's maintenance to the AI assistant. \
                    When a manufacturer manual is available for the vehicle's brand/model, the answer \
                    is grounded in it (RAG); otherwise the assistant answers with general knowledge and \
                    flags the reply as not manual-grounded. Conversation history is kept per user+vehicle \
                    for the lifetime of the running backend."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reply generated successfully.",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed (missing vehicleId or blank message).",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found or does not belong to the authenticated user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    public ChatResponse chat(Authentication authentication, @RequestBody @Valid ChatRequest request) {
        return chatService.ask(authentication.getName(), request);
    }
}
