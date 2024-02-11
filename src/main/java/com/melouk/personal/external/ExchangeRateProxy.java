package com.melouk.personal.external;

import com.melouk.personal.data.ExchangeRateRecord;
import com.melouk.personal.data.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.melouk.personal.external.ExchangeRateClient.API_EXCHANGE_RATE_PROVIDER;

@Component(ExchangeRateProxy.STORING_EXCHANGE_RATE_PROVIDER)
public class ExchangeRateProxy implements ExchangeRateProvider {
    public static final String STORING_EXCHANGE_RATE_PROVIDER = "storingExchangeRateProvider";
    private final ExchangeRateProvider exchangeRateClient;
    private final ExchangeRateAdapter exchangeRateAdapter;
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateProxy(@Qualifier(API_EXCHANGE_RATE_PROVIDER) ExchangeRateProvider exchangeRateClient,
                             ExchangeRateAdapter exchangeRateAdapter,
                             ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateClient = exchangeRateClient;
        this.exchangeRateAdapter = exchangeRateAdapter;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public ExchangeRateExternalResponse getLatestRateForSourceCurrency(String sourceCurrency) {
        Optional<ExchangeRateRecord> storedRate =
                exchangeRateRepository.findTopBySourceCurrencyOrderByTimeLastUpdateUnixDesc(sourceCurrency);

        return storedRate.map(exchangeRateAdapter::toExchangeRateResponse)
                .orElseGet(() -> getWithAPICall(sourceCurrency));
    }

    private ExchangeRateExternalResponse getWithAPICall(String sourceCurrency) {
        ExchangeRateExternalResponse response = exchangeRateClient.getLatestRateForSourceCurrency(sourceCurrency);
        saveToDatabase(sourceCurrency, response);
        return response;
    }

    private void saveToDatabase(String sourceCurrency, ExchangeRateExternalResponse response) {
        ExchangeRateRecord record = exchangeRateAdapter.toExchangeRateRecord(sourceCurrency, response);
        exchangeRateRepository.save(record);
    }

}
