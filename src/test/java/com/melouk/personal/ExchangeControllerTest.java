package com.melouk.personal;

import com.melouk.personal.ExchangeController.ExchangeRateExternalResponse;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(ExchangeController.class)
class ExchangeControllerTest {
    private static final String TIMESTAMP_PATTERN = "^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RestTemplate restTemplate;

    @ParameterizedTest(name = "Timestamp is {0}")
    @MethodSource
    void testConvertIsSuccessful(Long epoch, Matcher<?> timestampMatcher) throws Exception {
        String uri = "https://v6.exchangerate-api.com/v6/dummy_token/latest/USD";
        when(restTemplate.getForObject(uri, ExchangeRateExternalResponse.class))
                .thenReturn(new ExchangeRateExternalResponse(epoch, Map.of("EUR", 1.1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(5)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.from", equalTo("USD")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.to", equalTo("EUR")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.original", equalTo(10.0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.converted", equalTo(11.0)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp", timestampMatcher));
    }

    static Stream<Arguments> testConvertIsSuccessful() {
        var timestamp = ZonedDateTime.of(2024, 1, 1,
                        0, 0, 0, 0,
                        ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Stream.of(
                Arguments.of(1704067200L, equalTo(timestamp)),
                Arguments.of(null, matchesPattern(TIMESTAMP_PATTERN))
        );
    }

    @Test
    void testConvertFailsToFindSourceCurrency() throws Exception {
        String uri = "https://v6.exchangerate-api.com/v6/dummy_token/latest/USD";
        when(restTemplate.getForObject(uri, ExchangeRateExternalResponse.class)).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", equalTo(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", equalTo("Could not find rates for USD as a source currency.")));
    }

    @ParameterizedTest
    @MethodSource
    void testConvertFailsToFindTargetCurrency(Map<String, Double> rates) throws Exception {
        String uri = "https://v6.exchangerate-api.com/v6/dummy_token/latest/USD";
        when(restTemplate.getForObject(uri, ExchangeRateExternalResponse.class))
                .thenReturn(new ExchangeRateExternalResponse(1704067200L, rates));

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", equalTo(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", equalTo("Could not find rates for EUR as a target currency.")));
    }

    static Stream<Arguments> testConvertFailsToFindTargetCurrency() {
        return Stream.of(
                        Map.of(),
                        Map.of("JPY", 1.2)
                )
                .map(Arguments::of);
    }

    @Test
    void testConvertFailsToMakeExternalCall() throws Exception {
        String uri = "https://v6.exchangerate-api.com/v6/dummy_token/latest/USD";
        when(restTemplate.getForObject(uri, ExchangeRateExternalResponse.class))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", equalTo(500)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", equalTo("Something wrong happen, contact a specialist!")));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, -100})
    void testConvertFailsIfAmountIsNotPositive(double amount) throws Exception {
        String expectedMessage = "Parameter 'amount' needs to be greater than 0.";
        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", Double.toString(amount)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", equalTo(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", equalTo(expectedMessage)));
    }

    @ParameterizedTest(name = "Missing parameter {1}")
    @MethodSource
    void testConvertFailsForMissingInput(
            MultiValueMap<String, String> parameters,
            String missingParameter,
            String missingType) throws Exception {
        var expectedMessage =
                String.format("Required request parameter '%s' for method parameter type %s is not present",
                        missingParameter, missingType);
        mockMvc.perform(MockMvcRequestBuilders.get("/convert").params(parameters))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code", equalTo(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", equalTo(expectedMessage)));
    }

    static Stream<Arguments> testConvertFailsForMissingInput() {
        var parametersMissingAmount =
                new LinkedMultiValueMap<>(Map.of("from", List.of("EUR"), "to", List.of("USD")));

        var parametersMissingFrom =
                new LinkedMultiValueMap<>(Map.of("amount", List.of("10.0"), "to", List.of("USD")));

        var parametersMissingTo =
                new LinkedMultiValueMap<>(Map.of("from", List.of("EUR"), "amount", List.of("10.0")));
        return Stream.of(
                Arguments.of(parametersMissingAmount, "amount", "Double"),
                Arguments.of(parametersMissingFrom, "from", "String"),
                Arguments.of(parametersMissingTo, "to", "String")
        );
    }


}