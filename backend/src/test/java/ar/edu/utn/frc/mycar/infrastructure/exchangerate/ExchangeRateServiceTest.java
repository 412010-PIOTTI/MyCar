package ar.edu.utn.frc.mycar.infrastructure.exchangerate;

import ar.edu.utn.frc.mycar.web.dto.response.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateServiceTest {

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateService();
    }

    @Test
    void returnsEmptyWhenApiKeyNotConfigured() {
        ReflectionTestUtils.setField(exchangeRateService, "apiKey", "");

        Optional<ExchangeRateResponse> result = exchangeRateService.getTodayRate("USD", "ARS");

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenProviderCallFails() {
        ReflectionTestUtils.setField(exchangeRateService, "apiKey", "test-key");
        // Nothing listens here — the call fails fast and must be swallowed, not thrown.
        ReflectionTestUtils.setField(exchangeRateService, "baseUrl", "http://127.0.0.1:1");

        Optional<ExchangeRateResponse> result = exchangeRateService.getTodayRate("USD", "ARS");

        assertThat(result).isEmpty();
    }
}
