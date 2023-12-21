package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "foreign_trading", schema = "vnstock")
public class ForeignTradingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "buy_volume", nullable = false)
    private Double buyVolume;

    @Column(name = "sell_volume", nullable = false)
    private Double sellVolume;

    @Column(name = "buy_value", nullable = false)
    private Double buyValue;

    @Column(name = "sell_value", nullable = false)
    private Double sellValue;

    @Column(name = "trading_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate tradingDate;

    @Column(name = "hash_date", length = 255, nullable = false)
    private String hashDate;

    // Constructors, getters, and setters

    // Constructors
    public ForeignTradingEntity() {
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

    public Double getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(Double buyVolume) {
        this.buyVolume = buyVolume;
    }

    public Double getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(Double sellVolume) {
        this.sellVolume = sellVolume;
    }

    public Double getBuyValue() {
        return buyValue;
    }

    public void setBuyValue(Double buyValue) {
        this.buyValue = buyValue;
    }

    public Double getSellValue() {
        return sellValue;
    }

    public void setSellValue(Double sellValue) {
        this.sellValue = sellValue;
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
}
