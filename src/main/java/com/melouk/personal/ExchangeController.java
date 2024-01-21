package com.melouk.personal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
public class ExchangeController {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${API_TOKEN:dummy_token}")
    private String token;

    @GetMapping("/convert")
    public ConversionResult convert(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("amount") Double amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Parameter 'amount' needs to be greater than 0.");
        }
        var uri = String.format("https://v6.exchangerate-api.com/v6/%s/latest/%s", token, from);
        var rateResponse = restTemplate.getForObject(uri, ExchangeRateExternalResponse.class);
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

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler({MissingServletRequestParameterException.class, IllegalArgumentException.class})
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<Problem> handleInputException(Exception e) {
            return new ResponseEntity<>(new Problem(e.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalStateException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<Problem> handleNotFoundException(Exception e) {
            return new ResponseEntity<>(new Problem(e.getMessage(), HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<Problem> handleOtherException(Exception e) {
            return new ResponseEntity<>(new Problem("Something wrong happen, contact a specialist!",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public record ConversionResult(String from, String to, ZonedDateTime timestamp, double original, double converted) {
    }

    public record Problem(String message, int code) {
    }

    public record ExchangeRateExternalResponse(@JsonProperty("time_last_update_unix") Long timeLastUpdateUnix,
                                               @JsonProperty("conversion_rates") Map<String, Double> conversionRates) {
    }
}

