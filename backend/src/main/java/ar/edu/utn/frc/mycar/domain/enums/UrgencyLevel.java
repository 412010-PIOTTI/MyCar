package ar.edu.utn.frc.mycar.domain.enums;

public enum UrgencyLevel {
    /** Faltan 30+ días o 1000+ km */
    INFORMATIVA,
    /** Faltan entre 1 y 30 días o hasta 1000 km */
    ADVERTENCIA,
    /** Vencida o km superado */
    URGENTE
}
