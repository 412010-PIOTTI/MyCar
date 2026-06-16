package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a password reset token exists but its expiration time has passed. */
public class ResetTokenExpiredException extends RuntimeException {
    public ResetTokenExpiredException() {
        super("El enlace de recuperación expiró. Solicitá uno nuevo.");
    }
}
