package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.MaintenanceLog;
import ar.edu.utn.frc.mycar.domain.entity.TransferLog;
import ar.edu.utn.frc.mycar.domain.entity.TransferToken;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.MaintenanceSystem;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.enums.TransferStatus;
import ar.edu.utn.frc.mycar.domain.repository.MaintenanceLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.TransferLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.TransferTokenRepository;
import ar.edu.utn.frc.mycar.web.dto.response.TransferConfirmResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferGenerateResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferHistoryItemResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferPreviewResponse;
import ar.edu.utn.frc.mycar.web.exception.CannotTransferToSelfException;
import ar.edu.utn.frc.mycar.web.exception.TransferTokenInvalidException;
import ar.edu.utn.frc.mycar.web.exception.TransferTokenNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock TransferTokenRepository transferTokenRepository;
    @Mock TransferLogRepository transferLogRepository;
    @Mock MaintenanceLogRepository maintenanceLogRepository;
    @Mock VehicleService vehicleService;
    @Mock UserService userService;
    @Mock AlertService alertService;
    @Mock QrCodeService qrCodeService;

    @InjectMocks TransferService transferService;

    static final String SELLER_EMAIL = "seller@example.com";
    static final String BUYER_EMAIL = "buyer@example.com";
    static final Long VEHICLE_ID = 1L;
    static final String TOKEN = "sample-transfer-token";

    User seller;
    User buyer;
    Vehicle vehicle;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transferService, "frontendUrl", "http://localhost:4200");

        seller = User.builder().id(1L).name("Seller").email(SELLER_EMAIL).role(Role.USER).build();
        buyer = User.builder().id(2L).name("Buyer").email(BUYER_EMAIL).role(Role.USER).build();
        vehicle = Vehicle.builder()
                .id(VEHICLE_ID).owner(seller)
                .plate("AB123CD").brand("Toyota").model("Corolla")
                .year(2020).color("Blanco").currentKm(30000).active(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();
    }

    private TransferToken buildToken(String token, boolean used, LocalDateTime expiresAt) {
        return TransferToken.builder()
                .id(100L).vehicle(vehicle).generatedBy(seller)
                .token(token).used(used).expiresAt(expiresAt)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();
    }

    // ── generate ──────────────────────────────────────────────────────────────

    @Test
    void generate_createsTokenAndReturnsQr() {
        when(vehicleService.getEntity(VEHICLE_ID, SELLER_EMAIL)).thenReturn(vehicle);
        when(userService.getEntity(SELLER_EMAIL)).thenReturn(seller);
        when(transferTokenRepository.findByVehicleIdAndUsedFalse(VEHICLE_ID)).thenReturn(List.of());
        when(qrCodeService.generateBase64Png(anyString(), anyInt())).thenReturn("data:image/png;base64,AAAA");

        TransferGenerateResponse result = transferService.generate(SELLER_EMAIL, VEHICLE_ID);

        assertThat(result.token()).isNotBlank();
        assertThat(result.transferUrl()).isEqualTo("http://localhost:4200/transfer/confirm?token=" + result.token());
        assertThat(result.qrCodeBase64()).isEqualTo("data:image/png;base64,AAAA");
        assertThat(result.expiresAt()).isAfter(LocalDateTime.now().plusHours(47));
        verify(transferTokenRepository).save(any(TransferToken.class));
        verify(alertService).createAutoAlert(eq(vehicle), eq(seller), anyString(), eq(AlertType.DATE), any(LocalDate.class), eq(null));
    }

    @Test
    void generate_invalidatesPreviousPendingTokens() {
        TransferToken previous = buildToken("old-token", false, LocalDateTime.now().plusHours(10));
        when(vehicleService.getEntity(VEHICLE_ID, SELLER_EMAIL)).thenReturn(vehicle);
        when(userService.getEntity(SELLER_EMAIL)).thenReturn(seller);
        when(transferTokenRepository.findByVehicleIdAndUsedFalse(VEHICLE_ID)).thenReturn(List.of(previous));
        when(qrCodeService.generateBase64Png(anyString(), anyInt())).thenReturn("data:image/png;base64,AAAA");

        transferService.generate(SELLER_EMAIL, VEHICLE_ID);

        assertThat(previous.isUsed()).isTrue();
        verify(transferTokenRepository).saveAll(List.of(previous));
    }

    @Test
    void generate_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, SELLER_EMAIL)).thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        assertThatThrownBy(() -> transferService.generate(SELLER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getActiveToken ────────────────────────────────────────────────────────

    @Test
    void getActiveToken_withPendingToken_returnsIt() {
        TransferToken active = buildToken(TOKEN, false, LocalDateTime.now().plusHours(40));
        when(vehicleService.getEntity(VEHICLE_ID, SELLER_EMAIL)).thenReturn(vehicle);
        when(transferTokenRepository.findByVehicleIdAndUsedFalse(VEHICLE_ID)).thenReturn(List.of(active));
        when(qrCodeService.generateBase64Png(anyString(), anyInt())).thenReturn("data:image/png;base64,AAAA");

        TransferGenerateResponse result = transferService.getActiveToken(SELLER_EMAIL, VEHICLE_ID);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(TOKEN);
    }

    @Test
    void getActiveToken_noUnusedToken_returnsNull() {
        when(vehicleService.getEntity(VEHICLE_ID, SELLER_EMAIL)).thenReturn(vehicle);
        when(transferTokenRepository.findByVehicleIdAndUsedFalse(VEHICLE_ID)).thenReturn(List.of());

        TransferGenerateResponse result = transferService.getActiveToken(SELLER_EMAIL, VEHICLE_ID);

        assertThat(result).isNull();
    }

    @Test
    void getActiveToken_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, SELLER_EMAIL)).thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        assertThatThrownBy(() -> transferService.getActiveToken(SELLER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getHistory ────────────────────────────────────────────────────────────

    @Test
    void getHistory_reflectsPendingExpiredAndCompleted() {
        TransferToken pending = buildToken("pending-token", false, LocalDateTime.now().plusHours(10));
        TransferToken expired = buildToken("expired-token", false, LocalDateTime.now().minusHours(1));
        TransferToken completedToken = buildToken("completed-token", true, LocalDateTime.now().minusHours(2));

        TransferLog log = TransferLog.builder()
                .vehicle(vehicle).fromOwner(seller).toOwner(buyer)
                .token("completed-token").transferredAt(LocalDateTime.now().minusHours(1))
                .build();

        when(transferTokenRepository.findByGeneratedByEmailOrderByCreatedAtDesc(SELLER_EMAIL))
                .thenReturn(List.of(pending, expired, completedToken));
        when(transferLogRepository.findByToken("completed-token")).thenReturn(Optional.of(log));

        List<TransferHistoryItemResponse> result = transferService.getHistory(SELLER_EMAIL);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).status()).isEqualTo(TransferStatus.PENDING);
        assertThat(result.get(1).status()).isEqualTo(TransferStatus.EXPIRED);
        assertThat(result.get(2).status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(result.get(2).buyerName()).isEqualTo("Buyer");
    }

    @Test
    void getHistory_doesNotRequireCurrentVehicleOwnership() {
        when(transferTokenRepository.findByGeneratedByEmailOrderByCreatedAtDesc(SELLER_EMAIL)).thenReturn(List.of());

        List<TransferHistoryItemResponse> result = transferService.getHistory(SELLER_EMAIL);

        assertThat(result).isEmpty();
        verifyNoInteractions(vehicleService);
    }

    // ── preview ───────────────────────────────────────────────────────────────

    @Test
    void preview_validToken_returnsVehicleAndHistory() {
        TransferToken token = buildToken(TOKEN, false, LocalDateTime.now().plusHours(40));
        MaintenanceLog log = MaintenanceLog.builder()
                .id(5L).vehicle(vehicle).user(seller)
                .system(MaintenanceSystem.MOTOR).date(LocalDate.now()).kmAtMaintenance(30000)
                .createdAt(LocalDateTime.now())
                .build();

        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));
        when(maintenanceLogRepository.findByVehicleIdOrderByDateDescIdDesc(VEHICLE_ID)).thenReturn(List.of(log));

        TransferPreviewResponse result = transferService.preview(TOKEN);

        assertThat(result.vehiclePlate()).isEqualTo("AB123CD");
        assertThat(result.sellerName()).isEqualTo("Seller");
        assertThat(result.maintenanceHistory()).hasSize(1);
    }

    @Test
    void preview_tokenNotFound_throwsTransferTokenNotFoundException() {
        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.preview(TOKEN))
                .isInstanceOf(TransferTokenNotFoundException.class);
    }

    @Test
    void preview_expiredToken_throwsTransferTokenInvalidException() {
        TransferToken token = buildToken(TOKEN, false, LocalDateTime.now().minusMinutes(1));
        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> transferService.preview(TOKEN))
                .isInstanceOf(TransferTokenInvalidException.class);
    }

    @Test
    void preview_usedToken_throwsTransferTokenInvalidException() {
        TransferToken token = buildToken(TOKEN, true, LocalDateTime.now().plusHours(40));
        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> transferService.preview(TOKEN))
                .isInstanceOf(TransferTokenInvalidException.class);
    }

    // ── confirm ───────────────────────────────────────────────────────────────

    @Test
    void confirm_validToken_reassignsOwnershipAndLogsTransfer() {
        TransferToken token = buildToken(TOKEN, false, LocalDateTime.now().plusHours(40));
        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));
        when(userService.getEntity(BUYER_EMAIL)).thenReturn(buyer);

        TransferConfirmResponse result = transferService.confirm(BUYER_EMAIL, TOKEN);

        assertThat(vehicle.getOwner()).isEqualTo(buyer);
        assertThat(token.isUsed()).isTrue();
        assertThat(result.vehicleId()).isEqualTo(VEHICLE_ID);
        verify(transferLogRepository).save(any(TransferLog.class));
    }

    @Test
    void confirm_buyerIsCurrentOwner_throwsCannotTransferToSelfException() {
        TransferToken token = buildToken(TOKEN, false, LocalDateTime.now().plusHours(40));
        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));
        when(userService.getEntity(SELLER_EMAIL)).thenReturn(seller);

        assertThatThrownBy(() -> transferService.confirm(SELLER_EMAIL, TOKEN))
                .isInstanceOf(CannotTransferToSelfException.class);
        verify(transferLogRepository, never()).save(any());
    }

    @Test
    void confirm_expiredToken_throwsTransferTokenInvalidException() {
        TransferToken token = buildToken(TOKEN, false, LocalDateTime.now().minusMinutes(1));
        when(transferTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> transferService.confirm(BUYER_EMAIL, TOKEN))
                .isInstanceOf(TransferTokenInvalidException.class);
    }
}
