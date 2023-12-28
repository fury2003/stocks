package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "balance_sheet")
public class BalanceSheetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private Long equity;
    private Long totalAssets;
    private Long shortTermAssets;
    private Long longTermAssets;
    private Long liabilities;
    private Long currentLiabilities;
    private Long longTermLiabilities;
    private Long cashAndCashEquivalents;
    private Long shortTermInvestments;
    private Long accountsReceivable;
    private Long inventory;
    private String quarter;
    private String hashQuarter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Long getEquity() {
        return equity;
    }

    public void setEquity(Long equity) {
        this.equity = equity;
    }

    public Long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(Long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public Long getShortTermAssets() {
        return shortTermAssets;
    }

    public void setShortTermAssets(Long shortTermAssets) {
        this.shortTermAssets = shortTermAssets;
    }

    public Long getLongTermAssets() {
        return longTermAssets;
    }

    public void setLongTermAssets(Long longTermAssets) {
        this.longTermAssets = longTermAssets;
    }

    public Long getLiabilities() {
        return liabilities;
    }

    public void setLiabilities(Long liabilities) {
        this.liabilities = liabilities;
    }

    public Long getCurrentLiabilities() {
        return currentLiabilities;
    }

    public void setCurrentLiabilities(Long currentLiabilities) {
        this.currentLiabilities = currentLiabilities;
    }

    public Long getLongTermLiabilities() {
        return longTermLiabilities;
    }

    public void setLongTermLiabilities(Long longTermLiabilities) {
        this.longTermLiabilities = longTermLiabilities;
    }

    public Long getCashAndCashEquivalents() {
        return cashAndCashEquivalents;
    }

    public void setCashAndCashEquivalents(Long cashAndCashEquivalents) {
        this.cashAndCashEquivalents = cashAndCashEquivalents;
    }

    public Long getShortTermInvestments() {
        return shortTermInvestments;
    }

    public void setShortTermInvestments(Long shortTermInvestments) {
        this.shortTermInvestments = shortTermInvestments;
    }

    public Long getAccountsReceivable() {
        return accountsReceivable;
    }

    public void setAccountsReceivable(Long accountsReceivable) {
        this.accountsReceivable = accountsReceivable;
    }

    public Long getInventory() {
        return inventory;
    }

    public void setInventory(Long inventory) {
        this.inventory = inventory;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getHashQuarter() {
        return hashQuarter;
    }

    public void setHashQuarter(String hashQuarter) {
        this.hashQuarter = hashQuarter;
    }
}
