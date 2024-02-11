package com.melouk.personal.external;

import com.melouk.personal.data.ExchangeRateRecord;
import com.melouk.personal.data.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateProxyTest {

    private static final String USD = "USD";
    private static final ExchangeRateRecord EXCHANGE_RATE_RECORD =
            new ExchangeRateRecord(ZonedDateTime.now(), USD, Collections.emptyMap());
    private static final ExchangeRateExternalResponse EXCHANGE_RATE_EXTERNAL_RESPONSE =
            new ExchangeRateExternalResponse(Instant.now().getEpochSecond(), Collections.emptyMap());

    @Mock
    private ExchangeRateClient exchangeRateClient;
    @Mock
    private ExchangeRateAdapter exchangeRateAdapter;
    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateProxy exchangeRateProxy;

    @Test
    void testUsesRecordFromDatabase() {
        when(exchangeRateRepository.findTopBySourceCurrencyOrderByTimeLastUpdateUnixDesc(USD))
                .thenReturn(Optional.of(EXCHANGE_RATE_RECORD));
        when(exchangeRateAdapter.toExchangeRateResponse(EXCHANGE_RATE_RECORD))
                .thenReturn(EXCHANGE_RATE_EXTERNAL_RESPONSE);

        ExchangeRateExternalResponse test = exchangeRateProxy.getLatestRateForSourceCurrency(USD);

        assertThat(test).isEqualTo(EXCHANGE_RATE_EXTERNAL_RESPONSE);
        verifyNoInteractions(exchangeRateClient);
    }

    @Test
    void testGetsRecordFromAPIClient() {
        when(exchangeRateRepository.findTopBySourceCurrencyOrderByTimeLastUpdateUnixDesc(USD))
                .thenReturn(Optional.empty());
        when(exchangeRateClient.getLatestRateForSourceCurrency(USD))
                .thenReturn(EXCHANGE_RATE_EXTERNAL_RESPONSE);
        when(exchangeRateAdapter.toExchangeRateRecord(USD, EXCHANGE_RATE_EXTERNAL_RESPONSE))
                .thenReturn(EXCHANGE_RATE_RECORD);


        ExchangeRateExternalResponse test = exchangeRateProxy.getLatestRateForSourceCurrency(USD);

        assertThat(test).isEqualTo(EXCHANGE_RATE_EXTERNAL_RESPONSE);
        verify(exchangeRateRepository).save(EXCHANGE_RATE_RECORD);
    }
}