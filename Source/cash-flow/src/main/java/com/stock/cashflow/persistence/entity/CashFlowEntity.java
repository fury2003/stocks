package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cash_flow")
public class CashFlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private Long netCashFlowFromOperatingActivities;
    private Long netCashFlowFromInvestingActivities;
    private Long netCashFlowFromFinancingActivities;
    private Long netCashFlowForThePeriod;
    private Long beginningCashAndCashEquivalents;
    private Long endingCashAndCashEquivalents;
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

    public Long getNetCashFlowFromOperatingActivities() {
        return netCashFlowFromOperatingActivities;
    }

    public void setNetCashFlowFromOperatingActivities(Long netCashFlowFromOperatingActivities) {
        this.netCashFlowFromOperatingActivities = netCashFlowFromOperatingActivities;
    }

    public Long getNetCashFlowFromInvestingActivities() {
        return netCashFlowFromInvestingActivities;
    }

    public void setNetCashFlowFromInvestingActivities(Long netCashFlowFromInvestingActivities) {
        this.netCashFlowFromInvestingActivities = netCashFlowFromInvestingActivities;
    }

    public Long getNetCashFlowFromFinancingActivities() {
        return netCashFlowFromFinancingActivities;
    }

    public void setNetCashFlowFromFinancingActivities(Long netCashFlowFromFinancingActivities) {
        this.netCashFlowFromFinancingActivities = netCashFlowFromFinancingActivities;
    }

    public Long getBeginningCashAndCashEquivalents() {
        return beginningCashAndCashEquivalents;
    }

    public void setBeginningCashAndCashEquivalents(Long beginningCashAndCashEquivalents) {
        this.beginningCashAndCashEquivalents = beginningCashAndCashEquivalents;
    }

    public Long getEndingCashAndCashEquivalents() {
        return endingCashAndCashEquivalents;
    }

    public void setEndingCashAndCashEquivalents(Long endingCashAndCashEquivalents) {
        this.endingCashAndCashEquivalents = endingCashAndCashEquivalents;
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

    public Long getNetCashFlowForThePeriod() {
        return netCashFlowForThePeriod;
    }

    public void setNetCashFlowForThePeriod(Long netCashFlowForThePeriod) {
        this.netCashFlowForThePeriod = netCashFlowForThePeriod;
    }
}
