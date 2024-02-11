package com.melouk.personal.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component(ExchangeRateClient.API_EXCHANGE_RATE_PROVIDER)
public class ExchangeRateClient implements ExchangeRateProvider {
    public static final String API_EXCHANGE_RATE_PROVIDER = "apiExchangeRateProvider";
    private final String baseUri;
    private final String token;
    private final RestTemplate restTemplate;

    public ExchangeRateClient(
            @Value("${external.api.exchange_rate.uri}") String baseUri,
            @Value("${external.api.exchange_rate.token}") String token,
            RestTemplate restTemplate) {
        this.baseUri = baseUri;
        this.token = token;
        this.restTemplate = restTemplate;
    }

    @Override
    public ExchangeRateExternalResponse getLatestRateForSourceCurrency(String sourceCurrency) {
        var targetUri = UriComponentsBuilder.fromUriString(baseUri)
                .pathSegment(token, "latest", sourceCurrency)
                .toUriString();
        return restTemplate.getForObject(targetUri, ExchangeRateExternalResponse.class);
    }
}
