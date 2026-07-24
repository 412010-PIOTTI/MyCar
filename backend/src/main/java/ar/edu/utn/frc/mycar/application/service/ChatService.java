package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.infrastructure.ai.ManualCatalog;
import ar.edu.utn.frc.mycar.infrastructure.ai.ManualDefinition;
import ar.edu.utn.frc.mycar.web.dto.request.ChatRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Answers vehicle-maintenance questions, grounding the reply in the manufacturer manual that
 * matches the vehicle's brand/model when one is available (RAG), and falling back to general
 * knowledge — clearly labelled as such — otherwise.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int TOP_K = 4;

    private final VehicleService vehicleService;
    private final ManualCatalog manualCatalog;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ChatResponse ask(String ownerEmail, ChatRequest request) {
        Vehicle vehicle = vehicleService.getEntity(request.getVehicleId(), ownerEmail);
        String conversationId = ownerEmail + ":" + vehicle.getId();

        Optional<ManualDefinition> manual = manualCatalog.findFor(vehicle.getBrand(), vehicle.getModel());
        List<Document> context = manual.map(m -> retrieveContext(request.getMessage(), m)).orElse(List.of());
        boolean manualGrounded = !context.isEmpty();

        String reply = chatClient.prompt()
                .system(buildSystemPrompt(vehicle, manualGrounded, context))
                .user(request.getMessage())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return new ChatResponse(reply, manualGrounded, vehicle.getId());
    }

    private List<Document> retrieveContext(String question, ManualDefinition manual) {
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
        Filter.Expression filter = filterBuilder
                .and(filterBuilder.eq("brand", manual.brand()), filterBuilder.eq("model", manual.model()))
                .build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(TOP_K)
                .filterExpression(filter)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildSystemPrompt(Vehicle vehicle, boolean manualGrounded, List<Document> context) {
        String vehicleDescription = "%s %s %d".formatted(vehicle.getBrand(), vehicle.getModel(), vehicle.getYear());

        if (!manualGrounded) {
            return """
                    Sos el asistente técnico de MyCar para el vehículo %s.
                    No hay un manual del fabricante cargado para esta marca y modelo, así que respondé \
                    con tu conocimiento general de mantenimiento automotriz. Dejá explícitamente claro que \
                    la respuesta NO está basada en el manual específico de este vehículo y que conviene \
                    confirmarla con un mecánico o el manual impreso del auto.
                    """.formatted(vehicleDescription);
        }

        String manualExcerpts = context.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        return """
                Sos el asistente técnico de MyCar para el vehículo %s.
                Respondé la pregunta del usuario basándote en los siguientes fragmentos del manual \
                del fabricante de este vehículo. Si la respuesta no está en estos fragmentos, decilo \
                explícitamente en vez de inventar datos.

                Fragmentos del manual:
                %s
                """.formatted(vehicleDescription, manualExcerpts);
    }
}
