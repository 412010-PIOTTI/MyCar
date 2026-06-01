package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a login attempt fails due to an unknown email or wrong password. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
