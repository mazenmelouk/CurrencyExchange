package com.melouk.personal.contoller;

import com.melouk.personal.controller.ExchangeController;
import com.melouk.personal.service.ConversionResult;
import com.melouk.personal.service.ExchangeRateService;
import com.melouk.personal.service.ExchangeRequest;
import com.melouk.personal.service.NoRatesFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeController.class)
class ExchangeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void testConvertIsSuccessful() throws Exception {
        ConversionResult result = new ConversionResult("USD", "EUR", ZonedDateTime.now(), 10.0, 11.0);
        when(exchangeRateService.convert(ExchangeRequest.builder().originalAmount(10.0).sourceCurrency("USD").targetCurrency("EUR").build()))
                .thenReturn(result);

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$.from", equalTo(result.from())))
                .andExpect(jsonPath("$.to", equalTo(result.to())))
                .andExpect(jsonPath("$.original", equalTo(result.original())))
                .andExpect(jsonPath("$.converted", equalTo(result.converted())))
                .andExpect(jsonPath("$.timestamp", equalTo(result.timestamp().withFixedOffsetZone().toString())));
    }

    @Test
    void testConvertFailsToFindCurrency() throws Exception {
        when(exchangeRateService.convert(ArgumentMatchers.any(ExchangeRequest.class)))
                .thenThrow(new NoRatesFoundException("Error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.code", equalTo(404)))
                .andExpect(jsonPath("$.message", equalTo("Error")));
    }

    @Test
    void testConvertFails() throws Exception {
        when(exchangeRateService.convert(ArgumentMatchers.any(ExchangeRequest.class)))
                .thenThrow(RuntimeException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.code", equalTo(500)))
                .andExpect(jsonPath("$.message", equalTo("Something wrong happen, contact a specialist!")));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, -100})
    void testConvertFailsIfAmountIsNotPositive(double amount) throws Exception {
        String expectedMessage = "Parameter 'amount' needs to be greater than 0.";

        mockMvc.perform(MockMvcRequestBuilders.get("/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", Double.toString(amount)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.code", equalTo(400)))
                .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verifyNoInteractions(exchangeRateService);
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
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.code", equalTo(400)))
                .andExpect(jsonPath("$.message", equalTo(expectedMessage)));

        verifyNoInteractions(exchangeRateService);
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