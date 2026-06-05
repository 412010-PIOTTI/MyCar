package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
import ar.edu.utn.frc.mycar.web.exception.DuplicatePlateException;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Business logic for vehicle management.
 * Operates exclusively on DTOs; never exposes entities to the web layer.
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    /**
     * Registers a new vehicle for the authenticated user.
     *
     * <p>The plate is normalised to upper-case before persistence.
     * A single user may own multiple vehicles; each plate must be globally unique.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @param request    validated vehicle data from the request body
     * @return a read-only {@link VehicleResponse} representing the persisted vehicle
     * @throws DuplicatePlateException      if the plate is already registered
     * @throws InvalidCredentialsException  if no active user matches {@code ownerEmail}
     */
    @Transactional
    public VehicleResponse register(String ownerEmail, CreateVehicleRequest request) {
        String normalizedPlate = request.getPlate().toUpperCase(Locale.ROOT);

        if (vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new DuplicatePlateException(normalizedPlate);
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(InvalidCredentialsException::new);

        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .plate(normalizedPlate)
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .currentKm(request.getInitialKm())
                .build();

        return toResponse(vehicleRepository.save(vehicle));
    }

    /**
     * Returns a single vehicle by id, verifying it belongs to the authenticated user.
     *
     * <p>Returns 404 for both "vehicle does not exist" and "vehicle belongs to another user"
     * so as not to leak the existence of other users' vehicles.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @param id         vehicle primary key
     * @return the matching {@link VehicleResponse}
     * @throws VehicleNotFoundException if no vehicle with that id is owned by this user
     */
    @Transactional(readOnly = true)
    public VehicleResponse getById(String ownerEmail, Long id) {
        return vehicleRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    /**
     * Returns all vehicles owned by the authenticated user, ordered from newest to oldest.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @return list of {@link VehicleResponse}; empty list if the user has no vehicles
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAll(String ownerEmail) {
        return vehicleRepository.findByOwnerEmailOrderByCreatedAtDesc(ownerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getCurrentKm(),
                vehicle.getCreatedAt()
        );
    }
}
