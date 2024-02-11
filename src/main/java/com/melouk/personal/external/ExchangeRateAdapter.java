package com.melouk.personal.external;

import com.melouk.personal.data.ExchangeRateRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ExchangeRateAdapter {
    ExchangeRateExternalResponse toExchangeRateResponse(ExchangeRateRecord exchangeRateRecord) {
        return new ExchangeRateExternalResponse(
                exchangeRateRecord.getTimeLastUpdateUnix().toEpochSecond(),
                exchangeRateRecord.getConversionRates()
        );
    }

    ExchangeRateRecord toExchangeRateRecord(String sourceCurrency, ExchangeRateExternalResponse exchangeRateExternalResponse) {
        Instant instant = Instant.ofEpochSecond(exchangeRateExternalResponse.timeLastUpdateUnix());
        return new ExchangeRateRecord(
                ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")),
                sourceCurrency,
                exchangeRateExternalResponse.conversionRates());
    }
}
