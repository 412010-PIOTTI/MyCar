package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a login attempt is made for a disabled account. */
public class UserInactiveException extends RuntimeException {
    public UserInactiveException() {
        super("Account is disabled");
    }
}
