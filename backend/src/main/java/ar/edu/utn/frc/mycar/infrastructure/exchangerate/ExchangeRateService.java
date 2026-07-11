package ar.edu.utn.frc.mycar.infrastructure.exchangerate;

import ar.edu.utn.frc.mycar.web.dto.response.ExchangeRateResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/** Fetches the day's exchange rate quote (ExchangeRate API) for the CONCESIONARIO dashboard widget. */
@Service
@Slf4j
public class ExchangeRateService {

    @Value("${app.exchange-rate.api-key:}")
    private String apiKey;

    @Value("${app.exchange-rate.base-url:https://v6.exchangerate-api.com/v6}")
    private String baseUrl;

    private LocalDate cachedDate;
    private ExchangeRateResponse cachedRate;

    /** Returns today's quote, cached for the rest of the day. Empty if unconfigured or the provider fails. */
    public synchronized Optional<ExchangeRateResponse> getTodayRate(String baseCurrency, String targetCurrency) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Exchange rate API key not configured — skipping quote fetch");
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        if (cachedRate != null && today.equals(cachedDate)) {
            return Optional.of(cachedRate);
        }

        try {
            ExchangeRateApiResponse apiResponse = RestClient.builder().build()
                    .get()
                    .uri(baseUrl + "/{apiKey}/pair/{base}/{target}", apiKey, baseCurrency, targetCurrency)
                    .retrieve()
                    .body(ExchangeRateApiResponse.class);

            if (apiResponse == null || !"success".equals(apiResponse.result())) {
                log.warn("Exchange rate API returned no result for {}/{}", baseCurrency, targetCurrency);
                return Optional.empty();
            }

            ExchangeRateResponse rate = new ExchangeRateResponse(
                    baseCurrency, targetCurrency, apiResponse.conversionRate(), today);
            cachedDate = today;
            cachedRate = rate;
            return Optional.of(rate);
        } catch (Exception e) {
            log.error("Failed to fetch exchange rate {}/{}: {}", baseCurrency, targetCurrency, e.getMessage());
            return Optional.empty();
        }
    }

    private record ExchangeRateApiResponse(
            String result,
            @JsonProperty("conversion_rate") BigDecimal conversionRate
    ) {}
}
