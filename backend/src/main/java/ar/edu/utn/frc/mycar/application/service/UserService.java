package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.repository.AlertRepository;
import ar.edu.utn.frc.mycar.domain.repository.DocumentRepository;
import ar.edu.utn.frc.mycar.domain.repository.ExpenseRepository;
import ar.edu.utn.frc.mycar.domain.repository.MaintenanceLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.TransferLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.TransferTokenRepository;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.ChangePasswordRequest;
import ar.edu.utn.frc.mycar.web.dto.request.Toggle2FARequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateProfileRequest;
import ar.edu.utn.frc.mycar.web.dto.response.UserProfileResponse;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.PasswordMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ExpenseRepository expenseRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final DocumentRepository documentRepository;
    private final AlertRepository alertRepository;
    private final TransferTokenRepository transferTokenRepository;
    private final TransferLogRepository transferLogRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final RevokedTokenService revokedTokenService;

    @Transactional(readOnly = true)
    public UserProfileResponse getMe(String email) {
        User user = findByEmail(email);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        user.setName(request.getName());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Enables or disables 2FA for the user. Requires password confirmation.
     * Throws {@link PasswordMismatchException} if the password is wrong.
     */
    @Transactional
    public UserProfileResponse toggle2FA(String email, Toggle2FARequest request) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
        user.setTwoFactorEnabled(request.isEnabled());
        return toResponse(userRepository.save(user));
    }

    /**
     * Soft-deletes the account: sets {@code active=false} and revokes the current token.
     * Throws {@link PasswordMismatchException} if the provided password is incorrect.
     */
    @Transactional
    public void deleteAccount(String email, String password, String token) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
        user.setActive(false);
        userRepository.save(user);
        revokedTokenService.revokeToken(token, user);
    }

    /**
     * Permanently deletes the user's personal data (right-to-erasure request), distinct from the
     * soft {@link #deleteAccount}. For each vehicle the user owns:
     * <ul>
     *   <li>if it never appears in the permanent transfer audit trail, it is hard-deleted along
     *       with everything under it (expenses, maintenance logs, documents + files, alerts);</li>
     *   <li>otherwise the vehicle row itself is kept (transfer_logs.vehicle_id has no cascade and
     *       that history is intentionally immutable), but all its personal-data children are
     *       purged the same way.</li>
     * </ul>
     * The user's own name/email/password are then anonymized (the row is kept so any transfer_logs
     * referencing them as buyer/seller stay valid) and the account is deactivated and logged out.
     *
     * @throws PasswordMismatchException if the provided password is incorrect
     */
    @Transactional
    public void deleteMyData(String email, String password, String token) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }

        List<Vehicle> vehicles = new ArrayList<>(user.getVehicles());
        for (Vehicle vehicle : vehicles) {
            purgeVehiclePersonalData(vehicle);
        }
        transferTokenRepository.deleteByGeneratedByEmail(email);

        anonymize(user);
        userRepository.save(user);
        revokedTokenService.revokeToken(token, user);
    }

    /** Deletes a vehicle's PDF files and child rows, hard-deleting the vehicle too if it has no transfer history. */
    private void purgeVehiclePersonalData(Vehicle vehicle) {
        Long vehicleId = vehicle.getId();
        fileStorageService.deleteVehicleFolder(vehicleId);
        transferTokenRepository.deleteByVehicleId(vehicleId);

        if (transferLogRepository.existsByVehicleId(vehicleId)) {
            documentRepository.deleteByVehicleId(vehicleId);
            maintenanceLogRepository.deleteByVehicleId(vehicleId);
            expenseRepository.deleteByVehicleId(vehicleId);
            alertRepository.deleteByVehicleId(vehicleId);
        } else {
            vehicleRepository.delete(vehicle);
        }
    }

    private void anonymize(User user) {
        user.setName("Usuario eliminado");
        user.setEmail("deleted-" + user.getId() + "-" + UUID.randomUUID() + "@mycar.local");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setActive(false);
        user.setTwoFactorEnabled(false);
    }

    /**
     * Returns the {@link User} entity by email, for internal service-to-service use only.
     * Controllers must never call this method directly.
     *
     * @throws InvalidCredentialsException if no user matches the given email
     */
    @Transactional(readOnly = true)
    public User getEntity(String email) {
        return findByEmail(email);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.isTwoFactorEnabled()
        );
    }
}
