package com.melouk.personal.service;

import com.melouk.personal.external.ExchangeRateExternalResponse;
import com.melouk.personal.external.ExchangeRateProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.melouk.personal.external.ExchangeRateProxy.STORING_EXCHANGE_RATE_PROVIDER;

@Service
public class ExchangeRateService {

    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    private final ExchangeRateProvider exchangeRateClient;

    public ExchangeRateService(@Qualifier(STORING_EXCHANGE_RATE_PROVIDER)
                               ExchangeRateProvider exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public ConversionResult convert(ExchangeRequest request) throws NoRatesFoundException {
        String sourceCurrency = request.sourceCurrency();
        var rateResponse = exchangeRateClient.getLatestRateForSourceCurrency(sourceCurrency);
        var rateValue = extractRate(request, rateResponse);
        var epochTimestamp = extractTimestamp(rateResponse);
        double originalAmount = request.originalAmount();
        return new ConversionResult(sourceCurrency,
                request.targetCurrency(),
                epochTimestamp,
                originalAmount,
                originalAmount * rateValue);
    }

    private static double extractRate(ExchangeRequest request, ExchangeRateExternalResponse rateResponse) {
        var ratesMap = Optional.ofNullable(rateResponse)
                .map(ExchangeRateExternalResponse::conversionRates)
                .orElseThrow(() -> new NoRatesFoundException(String.format("Could not find rates for %s as a source currency.", request.sourceCurrency())));
        return Optional.ofNullable(ratesMap.get(request.targetCurrency()))
                .orElseThrow(() -> new NoRatesFoundException(String.format("Could not find rates for %s as a target currency.", request.targetCurrency())));
    }

    private static ZonedDateTime extractTimestamp(ExchangeRateExternalResponse rateResponse) {
        return Optional.of(rateResponse)
                .map(ExchangeRateExternalResponse::timeLastUpdateUnix)
                .map(Instant::ofEpochSecond)
                .map(instant -> ZonedDateTime.ofInstant(instant, UTC_ZONE_ID))
                .orElseGet(() -> ZonedDateTime.now(UTC_ZONE_ID))
                .withFixedOffsetZone();
    }
}
