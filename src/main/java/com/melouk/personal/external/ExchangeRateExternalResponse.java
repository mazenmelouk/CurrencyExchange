package com.melouk.personal.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ExchangeRateExternalResponse(@JsonProperty("time_last_update_unix") Long timeLastUpdateUnix,
                                           @JsonProperty("conversion_rates") Map<String, Double> conversionRates) {
}
