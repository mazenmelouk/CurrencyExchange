package com.melouk.personal.external;

import com.melouk.personal.data.ExchangeRateRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateAdapterTest {
    private static final String USD = "USD";
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ExchangeRateRecord EXCHANGE_RATE_RECORD =
            new ExchangeRateRecord(TIMESTAMP, USD, Collections.emptyMap());
    private static final ExchangeRateExternalResponse EXCHANGE_RATE_EXTERNAL_RESPONSE =
            new ExchangeRateExternalResponse(TIMESTAMP.toEpochSecond(), Collections.emptyMap());

    private ExchangeRateAdapter exchangeRateAdapter;

    @BeforeEach
    void setup() {
        exchangeRateAdapter = new ExchangeRateAdapter();
    }

    @Test
    void testConvertToExchangeRateRecord() {
        ExchangeRateRecord test = exchangeRateAdapter.toExchangeRateRecord(USD, EXCHANGE_RATE_EXTERNAL_RESPONSE);

        assertThat(test).isEqualTo(EXCHANGE_RATE_RECORD);
    }

    @Test
    void testConvertToExchangeRateResponse() {
        ExchangeRateExternalResponse test = exchangeRateAdapter.toExchangeRateResponse(EXCHANGE_RATE_RECORD);

        assertThat(test).isEqualTo(EXCHANGE_RATE_EXTERNAL_RESPONSE);
    }

}