package ar.edu.utn.frc.mycar.web.exception;

/** Thrown when an expense does not exist or does not belong to the requesting user's vehicle. */
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long id) {
        super("No se encontró un gasto con id " + id + " asociado a este vehículo.");
    }
}
