package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.ai.ManualCatalog;
import ar.edu.utn.frc.mycar.infrastructure.ai.ManualDefinition;
import ar.edu.utn.frc.mycar.web.dto.request.ChatRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ChatResponse;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock VehicleService vehicleService;
    @Mock ManualCatalog manualCatalog;
    @Mock VectorStore vectorStore;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;

    ChatService chatService;

    private static final String OWNER_EMAIL = "ana@example.com";
    private static final Long VEHICLE_ID = 1L;

    private Vehicle corolla;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(vehicleService, manualCatalog, vectorStore, chatClient);

        User owner = User.builder().id(1L).name("Ana Pérez").email(OWNER_EMAIL).role(Role.USER).build();
        corolla = Vehicle.builder()
                .id(VEHICLE_ID).owner(owner)
                .plate("AB123CD").brand("Toyota").model("Corolla").year(2020)
                .currentKm(35000).active(true)
                .build();
    }

    private void stubChatClientReply(String reply) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(reply);
    }

    @Test
    void ask_manualAvailableWithMatches_returnsGroundedReply() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(corolla);
        when(manualCatalog.findFor("Toyota", "Corolla"))
                .thenReturn(Optional.of(new ManualDefinition("Toyota", "Corolla", "manuals/toyota-corolla.md")));
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("Cambiar el aceite cada 10.000 km.")));
        stubChatClientReply("Cada 10.000 km o 12 meses.");

        ChatRequest request = new ChatRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setMessage("¿Cuándo toca el cambio de aceite?");

        ChatResponse response = chatService.ask(OWNER_EMAIL, request);

        assertThat(response.reply()).isEqualTo("Cada 10.000 km o 12 meses.");
        assertThat(response.manualGrounded()).isTrue();
        assertThat(response.vehicleId()).isEqualTo(VEHICLE_ID);
    }

    @Test
    void ask_noManualForBrandModel_returnsUngroundedReplyWithoutQueryingVectorStore() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(corolla);
        when(manualCatalog.findFor("Toyota", "Corolla")).thenReturn(Optional.empty());
        stubChatClientReply("No tengo el manual específico, pero en general...");

        ChatRequest request = new ChatRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setMessage("¿Qué aceite usa?");

        ChatResponse response = chatService.ask(OWNER_EMAIL, request);

        assertThat(response.manualGrounded()).isFalse();
        verifyNoInteractions(vectorStore);
    }

    @Test
    void ask_manualExistsButNoChunksMatch_returnsUngroundedReply() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(corolla);
        when(manualCatalog.findFor("Toyota", "Corolla"))
                .thenReturn(Optional.of(new ManualDefinition("Toyota", "Corolla", "manuals/toyota-corolla.md")));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        stubChatClientReply("No encontré esa info en el manual.");

        ChatRequest request = new ChatRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setMessage("¿Cómo se cambia la correa de distribución?");

        ChatResponse response = chatService.ask(OWNER_EMAIL, request);

        assertThat(response.manualGrounded()).isFalse();
    }

    @Test
    void ask_vehicleNotOwnedByUser_throwsVehicleNotFoundExceptionWithoutCallingChat() {
        when(vehicleService.getEntity(99L, OWNER_EMAIL)).thenThrow(new VehicleNotFoundException(99L));

        ChatRequest request = new ChatRequest();
        request.setVehicleId(99L);
        request.setMessage("¿Qué aceite usa?");

        assertThatThrownBy(() -> chatService.ask(OWNER_EMAIL, request))
                .isInstanceOf(VehicleNotFoundException.class);

        verifyNoInteractions(chatClient, vectorStore);
    }

    @Test
    void ask_conversationIdIsDerivedFromOwnerEmailAndVehicleId() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(corolla);
        when(manualCatalog.findFor("Toyota", "Corolla")).thenReturn(Optional.empty());
        stubChatClientReply("respuesta");

        ChatRequest request = new ChatRequest();
        request.setVehicleId(VEHICLE_ID);
        request.setMessage("hola");

        chatService.ask(OWNER_EMAIL, request);

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestSpec).advisors(captor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        when(advisorSpec.param(any(), any())).thenReturn(advisorSpec);
        captor.getValue().accept(advisorSpec);

        verify(advisorSpec).param(ChatMemory.CONVERSATION_ID, OWNER_EMAIL + ":" + VEHICLE_ID);
    }
}
