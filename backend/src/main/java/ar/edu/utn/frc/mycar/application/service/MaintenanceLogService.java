package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.MaintenanceLog;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.repository.MaintenanceLogRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateMaintenanceLogRequest;
import ar.edu.utn.frc.mycar.web.dto.response.MaintenanceLogResponse;
import ar.edu.utn.frc.mycar.web.exception.MaintenanceLogNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleService vehicleService;
    private final UserService userService;

    @Transactional
    public MaintenanceLogResponse create(String ownerEmail, Long vehicleId, CreateMaintenanceLogRequest request) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);

        if (request.getKmAtMaintenance() > vehicle.getCurrentKm()) {
            vehicle.setCurrentKm(request.getKmAtMaintenance());
        }

        User user = userService.getEntity(ownerEmail);
        MaintenanceLog log = MaintenanceLog.builder()
                .vehicle(vehicle)
                .user(user)
                .type(request.getType())
                .date(request.getDate())
                .kmAtMaintenance(request.getKmAtMaintenance())
                .description(request.getDescription())
                .cost(request.getCost())
                .build();

        return toResponse(maintenanceLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceLogResponse> getAll(String ownerEmail, Long vehicleId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        return maintenanceLogRepository
                .findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(vehicleId, ownerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceLogResponse getById(String ownerEmail, Long vehicleId, Long logId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        return maintenanceLogRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(logId, vehicleId, ownerEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new MaintenanceLogNotFoundException(logId));
    }

    private MaintenanceLogResponse toResponse(MaintenanceLog log) {
        return new MaintenanceLogResponse(
                log.getId(),
                log.getVehicle().getId(),
                log.getType(),
                log.getDate(),
                log.getKmAtMaintenance(),
                log.getDescription(),
                log.getCost(),
                log.getCreatedAt()
        );
    }
}
