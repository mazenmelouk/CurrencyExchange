package com.melouk.personal.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExchangeRateRepositoryIT {

    private static final Map<String, Double> USD_TO_EUR = Map.of("EUR", 1.0);
    private static final String USD = "USD";
    @Autowired
    private ExchangeRateRepository rateRepository;

    @Test
    void testFindLatestForSameSourceCurrency() {
        ExchangeRateRecord yesterdayRate =
                new ExchangeRateRecord(ZonedDateTime.now().minusDays(1), USD, USD_TO_EUR);

        rateRepository.save(yesterdayRate);
        ExchangeRateRecord todayRate =
                new ExchangeRateRecord(ZonedDateTime.now(), USD, USD_TO_EUR);
        ExchangeRateRecord expected = rateRepository.save(todayRate);
        Optional<ExchangeRateRecord> test = rateRepository.findTopBySourceCurrencyOrderByTimeLastUpdateUnixDesc(USD);
        assertThat(test).isPresent().hasValue(expected);
    }

    @Test
    void testReturnsEmptyForNonPersistedRates() {
        Optional<ExchangeRateRecord> test = rateRepository.findTopBySourceCurrencyOrderByTimeLastUpdateUnixDesc("EUR");

        assertThat(test).isEmpty();
    }
}