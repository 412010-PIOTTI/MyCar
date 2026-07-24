package ar.edu.utn.frc.mycar.infrastructure.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Catalog of manufacturer manuals available for RAG, keyed by brand+model.
 *
 * <p>Kept as a plain in-code list rather than a database table: at thesis-project scope there
 * are only two manuals, both static, and there is no migration tool (Flyway/Liquibase) in this
 * project to justify a new table for two rows.
 */
@Component
public class ManualCatalog {

    private static final List<ManualDefinition> MANUALS = List.of(
            new ManualDefinition("Toyota", "Corolla", "manuals/toyota-corolla.md"),
            new ManualDefinition("Ford", "Focus", "manuals/ford-focus.md")
    );

    public List<ManualDefinition> all() {
        return MANUALS;
    }

    public Optional<ManualDefinition> findFor(String brand, String model) {
        return MANUALS.stream()
                .filter(m -> m.brand().equalsIgnoreCase(normalized(brand))
                        && m.model().equalsIgnoreCase(normalized(model)))
                .findFirst();
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim();
    }
}
