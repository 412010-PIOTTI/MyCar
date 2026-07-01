package ar.edu.utn.frc.mycar.web.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long id) {
        super("No se encontró un documento con id " + id + " asociado a este vehículo.");
    }
}
