package com.melouk.personal.acceptance_tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class CurrencyExchangeStepsDefinition {

    private static final ParameterizedTypeReference<Map<String, String>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };
    private final WireMockServer mockExchangeServer;
    private final String externalAPIAccessToken;
    private final RestTemplate restTemplate;

    private ResponseEntity<Map<String, String>> result;

    public CurrencyExchangeStepsDefinition(WireMockServer mockExchangeServer,
                                           @Value("${external.api.exchange_rate.token}") String externalAPIAccessToken,
                                           RestTemplate restTemplate) {
        this.mockExchangeServer = mockExchangeServer;
        this.externalAPIAccessToken = externalAPIAccessToken;
        this.restTemplate = restTemplate;
    }

    @Given("the exchange rates for {word} are")
    public void mockExchangeRatesFor(String sourceCurrency, List<ExchangeRateTestEntry> rates) {
        var ratesAsString = "{" +
                rates.stream()
                        .map(rate -> "\"" + rate.targetCurrency() + "\"" + ":" + rate.rate())
                        .collect(joining(","))
                + "}";

        var responseBody = """
                                {
                                "result": "success",
                                "documentation": "https://www.exchangerate-api.com/docs",
                                "terms_of_use": "https://www.exchangerate-api.com/terms",
                                "time_last_update_unix": 1708128002,
                                "time_last_update_utc": "Sat, 17 Feb 2024 00:00:02 +0000",
                                "time_next_update_unix": 1708214402,
                                "time_next_update_utc": "Sun, 18 Feb 2024 00:00:02 +0000",
                                "base_code": "%s",
                                "conversion_rates": %s
                                }
                """.formatted(sourceCurrency, ratesAsString);
        mockExchangeServer.stubFor(get(
                urlPathEqualTo("/" + externalAPIAccessToken + "/latest/" + sourceCurrency)
        ).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(responseBody).withStatus(200)));
    }

    @When("I request to exchange {double} {word} to {word}")
    public void triggerQuery(double toConvert, String source, String target) {
        String uri = "http://localhost:8080/convert?from=%s&to=%s&amount=%s".formatted(source, target, toConvert);
        RequestEntity<Void> request = RequestEntity.get(uri).build();
        result = errorHandlingQuery(request);
    }

    private ResponseEntity<Map<String, String>> errorHandlingQuery(RequestEntity<Void> request) {
        try {
            return restTemplate.exchange(request, RESPONSE_TYPE);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).body(Map.of());
        }
    }

    @Then("response is {double} {word}")
    public void checkResponseValue(double expected, String target) {
        Map<String, String> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("converted"))
                .isNotNull()
                .isEqualTo(String.valueOf(expected));
        assertThat(body.get("to"))
                .isNotNull()
                .isEqualTo(target);
    }

    @Then("the request succeeds with status {int}")
    public void checkResponseStatusCode(int statusCode) {
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().value()).isEqualTo(statusCode);
    }

    @DataTableType
    @SuppressWarnings("unused")
    public ExchangeRateTestEntry convert(Map<String, String> tableRow) {
        return new ExchangeRateTestEntry(
                tableRow.get("currency"),
                Double.parseDouble(tableRow.get("rate"))
        );
    }

    public record ExchangeRateTestEntry(String targetCurrency, double rate) {
    }
}
