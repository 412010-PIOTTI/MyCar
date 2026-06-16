package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a password reset token does not exist or has already been used. */
public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("El enlace de recuperación es inválido o ya fue utilizado.");
    }
}
