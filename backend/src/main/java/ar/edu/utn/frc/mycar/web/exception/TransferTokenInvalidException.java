package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a transfer token exists but is expired or was already used. */
public class TransferTokenInvalidException extends RuntimeException {
    public TransferTokenInvalidException(String message) {
        super(message);
    }
}
