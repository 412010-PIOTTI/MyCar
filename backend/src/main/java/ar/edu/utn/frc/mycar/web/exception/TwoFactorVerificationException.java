package ar.edu.utn.frc.mycar.web.exception;

public class TwoFactorVerificationException extends RuntimeException {
    public TwoFactorVerificationException(String message) {
        super(message);
    }
}
