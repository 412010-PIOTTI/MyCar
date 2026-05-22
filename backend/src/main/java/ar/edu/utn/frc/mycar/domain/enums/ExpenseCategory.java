package ar.edu.utn.frc.mycar.domain.enums;

public enum ExpenseCategory {
    /** Combustible, peaje, lavado, estacionamiento, cochera */
    OPERATIVO,
    /** Service, neumáticos, distribución, aceite, mecánico */
    MANTENIMIENTO,
    /** Patente, seguro, ITV */
    IMPUESTO_SEGURO,
    /** Multas, grúa */
    INFRACCION,
    /** Alarma, car audio, car detail, accesorios */
    MEJORA,
    /** Solo CONCESIONARIO: transferencia, verificación vehicular, gestoría */
    ADMINISTRATIVO
}
