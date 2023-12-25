package com.stock.cashflow.persistence.entity;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "derivatives_trading", schema = "vnstock")
public class DerivativesTradingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "foreign_buy_volume")
    private int foreignBuyVolume;

    @Column(name = "foreign_sell_volume")
    private int foreignSellVolume;

    @Column(name = "foreign_net_volume")
    private int foreignNetVolume;

    @Column(name = "foreign_buy_value")
    private Double foreignBuyValue;

    @Column(name = "foreign_sell_value")
    private Double foreignSellValue;

    @Column(name = "foreign_net_value")
    private Double foreignNetValue;

    @Column(name = "proprietary_buy_volume")
    private int proprietaryBuyVolume;

    @Column(name = "proprietary_sell_volume")
    private int proprietarySellVolume;

    @Column(name = "proprietary_net_volume")
    private int proprietaryNetVolume;

    @Column(name = "proprietary_buy_value")
    private Double proprietaryBuyValue;

    @Column(name = "proprietary_sell_value")
    private Double proprietarySellValue;

    @Column(name = "proprietary_net_value")
    private Double proprietaryNetValue;

    @Column(name = "total_volume", nullable = false)
    private Double totalVolume;

    @Column(name = "open_interest")
    private int openInterest;

    @Column(name = "percentage_change", nullable = false, length = 10)
    private String percentageChange;

    @Column(name = "price_change", nullable = false)
    private Double priceChange;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "hash_date", nullable = false, length = 255)
    private String hashDate;

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

    public int getForeignBuyVolume() {
        return foreignBuyVolume;
    }

    public void setForeignBuyVolume(int foreignBuyVolume) {
        this.foreignBuyVolume = foreignBuyVolume;
    }

    public int getForeignSellVolume() {
        return foreignSellVolume;
    }

    public void setForeignSellVolume(int foreignSellVolume) {
        this.foreignSellVolume = foreignSellVolume;
    }

    public Double getForeignBuyValue() {
        return foreignBuyValue;
    }

    public void setForeignBuyValue(Double foreignBuyValue) {
        this.foreignBuyValue = foreignBuyValue;
    }

    public Double getForeignSellValue() {
        return foreignSellValue;
    }

    public void setForeignSellValue(Double foreignSellValue) {
        this.foreignSellValue = foreignSellValue;
    }

    public int getProprietaryBuyVolume() {
        return proprietaryBuyVolume;
    }

    public void setProprietaryBuyVolume(int proprietaryBuyVolume) {
        this.proprietaryBuyVolume = proprietaryBuyVolume;
    }

    public int getProprietarySellVolume() {
        return proprietarySellVolume;
    }

    public void setProprietarySellVolume(int proprietarySellVolume) {
        this.proprietarySellVolume = proprietarySellVolume;
    }

    public Double getProprietaryBuyValue() {
        return proprietaryBuyValue;
    }

    public void setProprietaryBuyValue(Double proprietaryBuyValue) {
        this.proprietaryBuyValue = proprietaryBuyValue;
    }

    public Double getProprietarySellValue() {
        return proprietarySellValue;
    }

    public void setProprietarySellValue(Double proprietarySellValue) {
        this.proprietarySellValue = proprietarySellValue;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public int getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(int openInterest) {
        this.openInterest = openInterest;
    }

    public String getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(String percentageChange) {
        this.percentageChange = percentageChange;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
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

    public int getForeignNetVolume() {
        return foreignNetVolume;
    }

    public void setForeignNetVolume(int foreignNetVolume) {
        this.foreignNetVolume = foreignNetVolume;
    }

    public Double getForeignNetValue() {
        return foreignNetValue;
    }

    public void setForeignNetValue(Double foreignNetValue) {
        this.foreignNetValue = foreignNetValue;
    }

    public int getProprietaryNetVolume() {
        return proprietaryNetVolume;
    }

    public void setProprietaryNetVolume(int proprietaryNetVolume) {
        this.proprietaryNetVolume = proprietaryNetVolume;
    }

    public Double getProprietaryNetValue() {
        return proprietaryNetValue;
    }

    public void setProprietaryNetValue(Double proprietaryNetValue) {
        this.proprietaryNetValue = proprietaryNetValue;
    }
}
