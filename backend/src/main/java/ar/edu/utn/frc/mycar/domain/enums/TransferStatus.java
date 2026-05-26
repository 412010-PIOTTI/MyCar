package ar.edu.utn.frc.mycar.domain.enums;

public enum TransferStatus {
    /** Token generado, aún no usado */
    ACTIVE,
    /** Token utilizado — transferencia completada */
    USED,
    /** Token expirado (48hs sin uso) */
    EXPIRED,
    /** Cancelado por el propietario */
    CANCELLED
}
