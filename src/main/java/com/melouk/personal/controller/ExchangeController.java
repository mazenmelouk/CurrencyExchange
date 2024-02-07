package com.melouk.personal.controller;

import com.melouk.personal.service.ConversionResult;
import com.melouk.personal.service.ExchangeRateService;
import com.melouk.personal.service.ExchangeRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExchangeController {
    private final ExchangeRateService exchangeRateService;

    public ExchangeController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
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
        var exchangeRequest = ExchangeRequest.builder()
                .sourceCurrency(from)
                .targetCurrency(to)
                .originalAmount(amount)
                .build();
        return exchangeRateService.convert(exchangeRequest);
    }

    public record Problem(String message, int code) {
    }
}

