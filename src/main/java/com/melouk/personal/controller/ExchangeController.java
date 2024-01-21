package com.melouk.personal.controller;

import com.melouk.personal.external.ExchangeRateClient;
import com.melouk.personal.external.ExchangeRateExternalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@RestController
public class ExchangeController {
    private final ExchangeRateClient exchangeRateClient;

    public ExchangeController(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    @GetMapping("/convert")
    public ConversionResult convert(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("amount") Double amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Parameter 'amount' needs to be greater than 0.");
        }
        var rateResponse = exchangeRateClient.getLatestRateForSourceCurrency(from);
        var ratesMap = Optional.ofNullable(rateResponse)
                .map(ExchangeRateExternalResponse::conversionRates)
                .orElseThrow(() -> new IllegalStateException(String.format("Could not find rates for %s as a source currency.", from)));
        var rateValue = Optional.ofNullable(ratesMap.get(to))
                .orElseThrow(() -> new IllegalStateException(String.format("Could not find rates for %s as a target currency.", to)));
        var epochTimestamp = Optional.of(rateResponse)
                .map(ExchangeRateExternalResponse::timeLastUpdateUnix)
                .map(Instant::ofEpochSecond)
                .map(instant -> ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")))
                .orElseGet(() -> ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")));
        return new ConversionResult(from, to, epochTimestamp, amount, amount * rateValue);
    }

    public record ConversionResult(String from, String to, ZonedDateTime timestamp, double original, double converted) {
    }

    public record Problem(String message, int code) {
    }
}

