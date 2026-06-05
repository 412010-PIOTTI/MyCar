package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a vehicle with the given plate is already registered in the system. */
public class DuplicatePlateException extends RuntimeException {

    public DuplicatePlateException(String plate) {
        super("La patente '" + plate + "' ya está registrada en el sistema.");
    }
}
