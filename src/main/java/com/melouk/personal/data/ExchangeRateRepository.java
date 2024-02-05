package com.melouk.personal.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateRecord, Long> {

    Optional<ExchangeRateRecord> findTopBySourceCurrencyOrderByTimeLastUpdateUnixDesc(String sourceCurrency);
}
