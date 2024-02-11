package com.melouk.personal.data;



import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "exchange_rate_record")
public final class ExchangeRateRecord {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    private final ZonedDateTime timeLastUpdateUnix;
    @OrderColumn(name = "source_currency")
    private final String sourceCurrency;
    @ElementCollection
    @CollectionTable(name = "exchange_rate_mapping",
            joinColumns = {@JoinColumn(name = "exchange_rate_record_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "target_currency")
    @Column(name = "rate")
    private final Map<String, Double> conversionRates;

    ExchangeRateRecord(){
        this(ZonedDateTime.now(),null, Collections.emptyMap());
    }
    public ExchangeRateRecord(ZonedDateTime timeLastUpdateUnix,
                              String sourceCurrency,
                              Map<String, Double> conversionRates) {
        this.timeLastUpdateUnix = ZonedDateTime.from(timeLastUpdateUnix);
        this.sourceCurrency = sourceCurrency;
        this.conversionRates = Map.copyOf(conversionRates);
    }

    public long getId() {
        return id;
    }

    public ZonedDateTime getTimeLastUpdateUnix() {
        return timeLastUpdateUnix;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRateRecord that = (ExchangeRateRecord) o;
        return id == that.id &&
                Objects.equals(timeLastUpdateUnix, that.timeLastUpdateUnix) &&
                Objects.equals(sourceCurrency, that.sourceCurrency) &&
                Objects.equals(conversionRates, that.conversionRates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeLastUpdateUnix, sourceCurrency, conversionRates);
    }
}
