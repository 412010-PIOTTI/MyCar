package ar.edu.utn.frc.mycar.domain.enums;

/** Computed at read time from {@code used}/{@code expiresAt}; never persisted. */
public enum TransferStatus {
    PENDING,
    EXPIRED,
    COMPLETED
}
