package com.melouk.personal.service;

import com.melouk.personal.external.ExchangeRateClient;
import com.melouk.personal.external.ExchangeRateExternalResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final ExchangeRequest REQUEST = ExchangeRequest.builder()
            .sourceCurrency(USD)
            .targetCurrency(EUR)
            .originalAmount(10).build();
    private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
            .withFixedOffsetZone();
    @Mock
    private ExchangeRateClient exchangeRateClient;
    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void testCovertsSuccessfully() {
        ExchangeRateExternalResponse response =
                new ExchangeRateExternalResponse(ZONED_DATE_TIME.toInstant().getEpochSecond(), Map.of(EUR, 1.1));
        when(exchangeRateClient.getLatestRateForSourceCurrency(USD)).thenReturn(response);

        ConversionResult test = exchangeRateService.convert(REQUEST);

        ConversionResult expected = new ConversionResult(USD, EUR, ZONED_DATE_TIME, 10, 11);

        assertThat(test).isEqualTo(expected);
    }

    @Test
    void testConvertsSuccessfullyWithFallbackTimestampToNow() {
        try (MockedStatic<ZonedDateTime> mockedStatic = mockStatic(ZonedDateTime.class)) {
            mockedStatic.when(() -> ZonedDateTime.now(ZoneId.of("UTC"))).thenReturn(ZONED_DATE_TIME);
            ExchangeRateExternalResponse response =
                    new ExchangeRateExternalResponse(null, Map.of(EUR, 1.1));
            when(exchangeRateClient.getLatestRateForSourceCurrency(USD)).thenReturn(response);

            ConversionResult test = exchangeRateService.convert(REQUEST);
            ConversionResult expected = new ConversionResult(USD, EUR, ZONED_DATE_TIME, 10, 11);

            assertThat(test).isEqualTo(expected);
        }
    }

    @Test
    void testFailsOnClientCall() {
        when(exchangeRateClient.getLatestRateForSourceCurrency(USD)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> exchangeRateService.convert(REQUEST));
    }

    @Test
    void testFailsOnMissingTargetCurrency() {
        ExchangeRateExternalResponse response =
                new ExchangeRateExternalResponse(ZONED_DATE_TIME.toInstant().getEpochSecond(), Map.of());
        when(exchangeRateClient.getLatestRateForSourceCurrency(USD)).thenReturn(response);

        assertThrows(NoRatesFoundException.class, () -> exchangeRateService.convert(REQUEST));
    }

    @Test
    void testFailsOnMissingSourceCurrency() {
        when(exchangeRateClient.getLatestRateForSourceCurrency(USD)).thenReturn(null);

        assertThrows(NoRatesFoundException.class, () -> exchangeRateService.convert(REQUEST));
    }
}