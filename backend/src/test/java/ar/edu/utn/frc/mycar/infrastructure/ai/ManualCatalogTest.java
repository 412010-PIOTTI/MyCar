package ar.edu.utn.frc.mycar.infrastructure.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualCatalogTest {

    private final ManualCatalog catalog = new ManualCatalog();

    @Test
    void findFor_knownBrandAndModel_returnsManual() {
        var manual = catalog.findFor("Toyota", "Corolla");

        assertThat(manual).isPresent();
        assertThat(manual.get().resourcePath()).isEqualTo("manuals/toyota-corolla.md");
    }

    @Test
    void findFor_isCaseInsensitive() {
        var manual = catalog.findFor("toyota", "COROLLA");

        assertThat(manual).isPresent();
    }

    @Test
    void findFor_unknownModel_returnsEmpty() {
        var manual = catalog.findFor("Toyota", "Camry");

        assertThat(manual).isEmpty();
    }

    @Test
    void findFor_knownBrandWrongModel_returnsEmpty() {
        var manual = catalog.findFor("Ford", "Corolla");

        assertThat(manual).isEmpty();
    }

    @Test
    void all_returnsBothManuals() {
        assertThat(catalog.all()).hasSize(2);
    }
}
