package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when the current password provided does not match the stored hash. */
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Current password is incorrect");
    }
}
