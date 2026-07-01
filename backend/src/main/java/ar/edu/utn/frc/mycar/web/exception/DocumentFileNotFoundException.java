package ar.edu.utn.frc.mycar.web.exception;

public class DocumentFileNotFoundException extends RuntimeException {

    public DocumentFileNotFoundException(Long documentId) {
        super("El documento con id " + documentId + " no tiene un archivo adjunto.");
    }
}
