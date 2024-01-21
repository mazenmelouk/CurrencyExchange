package com.melouk.personal.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ExchangeRateClientTest {
    private static final String BASE_URI = "some-base-uri";
    private static final String TOKEN = "someToken";
    @Mock
    private RestTemplate restTemplate;

    private ExchangeRateClient exchangeRateClient;

    @BeforeEach
    void setup() {
        exchangeRateClient = new ExchangeRateClient(BASE_URI, TOKEN, restTemplate);
    }

    @Test
    void testCallWithCorrectParameters() {
        var response = new ExchangeRateExternalResponse(1L, Map.of());
        when(restTemplate.getForObject("some-base-uri/someToken/latest/USD", ExchangeRateExternalResponse.class))
                .thenReturn(response);

        var test = exchangeRateClient.getLatestRateForSourceCurrency("USD");

        assertThat(test).isEqualTo(response);
    }

    @Test
    void testFailureThrowsException() {
        when(restTemplate.getForObject("some-base-uri/someToken/latest/USD", ExchangeRateExternalResponse.class))
                .thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> exchangeRateClient.getLatestRateForSourceCurrency("USD"));
    }
}