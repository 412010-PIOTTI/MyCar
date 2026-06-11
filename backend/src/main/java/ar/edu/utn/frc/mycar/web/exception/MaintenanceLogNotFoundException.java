package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a maintenance record does not exist or does not belong to the requesting user's vehicle. */
public class MaintenanceLogNotFoundException extends RuntimeException {

    public MaintenanceLogNotFoundException(Long id) {
        super("No se encontró un registro de mantenimiento con id " + id + " asociado a este vehículo.");
    }
}
