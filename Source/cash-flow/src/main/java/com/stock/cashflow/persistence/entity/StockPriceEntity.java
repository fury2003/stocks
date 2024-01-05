package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price", schema = "vnstock")
public class StockPriceEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "highest_price", nullable = false)
    private Double highestPrice;

    @Column(name = "lowest_price", nullable = false)
    private Double lowestPrice;

    @Column(name = "open_price", nullable = false)
    private Double openPrice;

    @Column(name = "close_price", nullable = false)
    private Double closePrice;

    @Column(name = "price_change", nullable = true)
    private Double priceChange;

    @Column(name = "percentage_change", nullable = true)
    private String percentageChange;

    @Column(name = "total_volume", nullable = false)
    private Double totalVolume;

    @Column(name = "price_range", nullable = true)
    private String priceRange;

    @Column(name = "trading_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate tradingDate;

    @Column(name = "hash_date", length = 255, nullable = false)
    private String hashDate;

    // Constructors, getters, and setters

    // Constructors
    public StockPriceEntity() {
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

    public Double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(Double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public Double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(Double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public Double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Double openPrice) {
        this.openPrice = openPrice;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
    }

    public String getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(String percentageChange) {
        this.percentageChange = percentageChange;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }
}
