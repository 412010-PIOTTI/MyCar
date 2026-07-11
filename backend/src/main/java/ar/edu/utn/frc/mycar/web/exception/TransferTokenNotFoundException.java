package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when a transfer token does not exist. */
public class TransferTokenNotFoundException extends RuntimeException {
    public TransferTokenNotFoundException() {
        super("El código de transferencia no existe.");
    }
}
