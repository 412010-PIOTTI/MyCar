package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a vehicle does not exist or does not belong to the requesting user. */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long id) {
        super("No se encontró un vehículo con id " + id + " asociado a tu cuenta.");
    }
}
