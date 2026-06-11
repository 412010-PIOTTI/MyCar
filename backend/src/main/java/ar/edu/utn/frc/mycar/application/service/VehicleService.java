package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
import ar.edu.utn.frc.mycar.web.exception.DuplicatePlateException;
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
    private final UserService userService;

    /**
     * Registers a new vehicle for the authenticated user.
     *
     * <p>The plate is normalised to upper-case before persistence.
     * A single user may own multiple vehicles; each plate must be globally unique.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @param request    validated vehicle data from the request body
     * @return a read-only {@link VehicleResponse} representing the persisted vehicle
     * @throws DuplicatePlateException if the plate is already registered
     */
    @Transactional
    public VehicleResponse register(String ownerEmail, CreateVehicleRequest request) {
        String normalizedPlate = request.getPlate().toUpperCase(Locale.ROOT);

        if (vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new DuplicatePlateException(normalizedPlate);
        }

        User owner = userService.getEntity(ownerEmail);

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
     * Updates the mutable fields of a vehicle owned by the authenticated user.
     *
     * <p>All request fields are optional. A {@code null} value means "leave unchanged".
     * The plate, if provided, is normalised to upper-case and validated for global uniqueness
     * (the vehicle's own current plate is excluded from the uniqueness check).
     * An empty {@code color} string is interpreted as "clear the color".
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @param id         vehicle primary key
     * @param request    fields to update; any {@code null} field is skipped
     * @return the updated {@link VehicleResponse}
     * @throws VehicleNotFoundException if no active vehicle with that id is owned by this user
     * @throws DuplicatePlateException  if the new plate is already taken by another vehicle
     */
    @Transactional
    public VehicleResponse update(String ownerEmail, Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(id, ownerEmail)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (request.getPlate() != null) {
            String normalizedPlate = request.getPlate().toUpperCase(Locale.ROOT);
            if (vehicleRepository.existsByPlateAndActiveTrueAndIdNot(normalizedPlate, id)) {
                throw new DuplicatePlateException(normalizedPlate);
            }
            vehicle.setPlate(normalizedPlate);
        }

        if (request.getBrand() != null) {
            vehicle.setBrand(request.getBrand());
        }

        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }

        if (request.getYear() != null) {
            vehicle.setYear(request.getYear());
        }

        if (request.getColor() != null) {
            vehicle.setColor(request.getColor().isBlank() ? null : request.getColor());
        }

        return toResponse(vehicle);
    }

    /**
     * Soft-deletes a vehicle by setting its {@code active} flag to {@code false}.
     *
     * <p>The vehicle immediately disappears from all listings and cannot be accessed
     * by any endpoint. The operation is irreversible via the public API.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @param id         vehicle primary key
     * @throws VehicleNotFoundException if no active vehicle with that id is owned by this user
     */
    @Transactional
    public void delete(String ownerEmail, Long id) {
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(id, ownerEmail)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        vehicle.setActive(false);
    }

    /**
     * Returns a single active vehicle by id, verifying it belongs to the authenticated user.
     *
     * <p>Returns 404 for "vehicle does not exist", "belongs to another user", and
     * "vehicle is soft-deleted" to avoid leaking existence information.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @param id         vehicle primary key
     * @return the matching {@link VehicleResponse}
     * @throws VehicleNotFoundException if no active vehicle with that id is owned by this user
     */
    @Transactional(readOnly = true)
    public VehicleResponse getById(String ownerEmail, Long id) {
        return vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(id, ownerEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    /**
     * Returns all active vehicles owned by the authenticated user, ordered from newest to oldest.
     *
     * @param ownerEmail email of the authenticated user (from JWT)
     * @return list of {@link VehicleResponse}; empty list if the user has no active vehicles
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAll(String ownerEmail) {
        return vehicleRepository.findByOwnerEmailAndActiveTrueOrderByCreatedAtDesc(ownerEmail)
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
