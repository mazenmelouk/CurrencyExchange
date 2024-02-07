package com.melouk.personal.service;

import java.time.ZonedDateTime;

public record ConversionResult(String from, String to, ZonedDateTime timestamp, double original, double converted) {
}
