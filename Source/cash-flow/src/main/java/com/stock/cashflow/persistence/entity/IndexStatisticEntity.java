package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "index_statistic", schema = "vnstock")
public class IndexStatisticEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "percentage_taken_on_index", nullable = true)
    private String percentageTakenOnIndex;

    @Column(name = "volume", nullable = false)
    private Double volume;

    @Column(name = "total_volume", nullable = false)
    private Double totalVolume;

    @Column(name = "trading_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate tradingDate;

    @Column(name = "hash_date", length = 255, nullable = false)
    private String hashDate;

    // Constructors, getters, and setters

    // Constructors
    public IndexStatisticEntity() {
        // Default constructor
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getHashDate() {
        return hashDate;
    }

    public void setHashDate(String hashDate) {
        this.hashDate = hashDate;
    }

    public String getPercentageTakenOnIndex() {
        return percentageTakenOnIndex;
    }

    public void setPercentageTakenOnIndex(String percentageTakenOnIndex) {
        this.percentageTakenOnIndex = percentageTakenOnIndex;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }
}
