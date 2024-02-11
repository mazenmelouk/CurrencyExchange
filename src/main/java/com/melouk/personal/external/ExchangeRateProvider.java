package com.melouk.personal.external;

public interface ExchangeRateProvider {
    ExchangeRateExternalResponse getLatestRateForSourceCurrency(String sourceCurrency);
}
