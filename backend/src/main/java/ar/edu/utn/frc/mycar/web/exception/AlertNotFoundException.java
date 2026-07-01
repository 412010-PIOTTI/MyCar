package ar.edu.utn.frc.mycar.web.exception;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long id) {
        super("No se encontró una alerta con id " + id + " asociada a este vehículo.");
    }
}
