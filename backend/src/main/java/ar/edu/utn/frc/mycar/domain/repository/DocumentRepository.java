package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Document;
import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(
            Long vehicleId, String ownerEmail);

    Optional<Document> findByIdAndVehicleIdAndVehicleOwnerEmail(
            Long id, Long vehicleId, String ownerEmail);

    Optional<Document> findByVehicleIdAndTypeAndActiveTrue(Long vehicleId, DocumentType type);
}
