package com.melouk.personal.service;

import lombok.Builder;

@Builder
public record ExchangeRequest(String sourceCurrency, String targetCurrency, double originalAmount) {
}
