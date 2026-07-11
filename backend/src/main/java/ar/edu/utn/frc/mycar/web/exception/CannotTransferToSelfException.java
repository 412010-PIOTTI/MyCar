package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when the scanning/confirming user is already the current owner of the vehicle. */
public class CannotTransferToSelfException extends RuntimeException {
    public CannotTransferToSelfException() {
        super("No podés transferir el vehículo a tu propia cuenta.");
    }
}
