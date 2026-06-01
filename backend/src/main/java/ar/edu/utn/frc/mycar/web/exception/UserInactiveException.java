package ar.edu.utn.frc.mycar.web.exception;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException() {
        super("Account is disabled");
    }
}
