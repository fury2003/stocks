package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "intraday_order", schema = "vnstock")
public class IntradayOrderEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "buy_volume", nullable = false)
    private Integer buyVolume;

    @Column(name = "sell_volume", nullable = false)
    private Integer sellVolume;

    @Column(name = "buy_order", nullable = false)
    private Integer buyOrder;

    @Column(name = "sell_order", nullable = false)
    private Integer sellOrder;

    @Column(name = "trading_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate tradingDate;

    @Column(name = "hash_date", length = 255, nullable = false)
    private String hashDate;

    // Constructors, getters, and setters

    // Constructors
    public IntradayOrderEntity() {
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

    public Integer getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(Integer buyVolume) {
        this.buyVolume = buyVolume;
    }

    public Integer getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(Integer sellVolume) {
        this.sellVolume = sellVolume;
    }

    public Integer getBuyOrder() {
        return buyOrder;
    }

    public void setBuyOrder(Integer buyOrder) {
        this.buyOrder = buyOrder;
    }

    public Integer getSellOrder() {
        return sellOrder;
    }

    public void setSellOrder(Integer sellOrder) {
        this.sellOrder = sellOrder;
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
