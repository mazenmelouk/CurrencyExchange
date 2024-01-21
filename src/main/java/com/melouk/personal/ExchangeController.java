package com.melouk.personal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
public class ExchangeController {

    @GetMapping("/convert")
    public ConversionResult convert(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("amount") Double amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Parameter 'amount' needs to be greater than 0.");
        }
        return new ConversionResult(from, to, ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), amount, amount * 1.1);
    }

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler({MissingServletRequestParameterException.class, IllegalArgumentException.class})
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<Problem> handleException(Exception e) {
            return new ResponseEntity<>(new Problem(e.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
        }
    }

    public record ConversionResult(String from, String to, ZonedDateTime timestamp, double original, double converted) {
    }

    public record Problem(String message, int code) {
    }
}
