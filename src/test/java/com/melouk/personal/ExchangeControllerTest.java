package com.melouk.personal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;

@WebMvcTest(ExchangeController.class)
class ExchangeControllerTest {
    private static final String TIMESTAMP_PATTERN = "^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$";
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testConvertIsSuccessful() throws Exception {
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
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