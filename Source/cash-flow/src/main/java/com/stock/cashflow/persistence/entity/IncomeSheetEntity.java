package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "income_sheet")
public class IncomeSheetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private Long netRevenue;
    private Long grossProfit;
    private Long cogs;
    private Long financeCharge;
    private Long interestCost;
    private Long sellingExpenses;
    private Long operatingExpenses;
    private Long financialActivitiesIncome;
    private Long otherIncome;
    private Long earningsBeforeTaxes;
    private Long corporateIncomeTax;
    private Long profitAfterTaxes;
    private Long netIncomeAttributableToParent;
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

    public Long getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(Long netRevenue) {
        this.netRevenue = netRevenue;
    }

    public Long getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(Long grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Long getCogs() {
        return cogs;
    }

    public void setCogs(Long cogs) {
        this.cogs = cogs;
    }

    public Long getFinanceCharge() {
        return financeCharge;
    }

    public void setFinanceCharge(Long financeCharge) {
        this.financeCharge = financeCharge;
    }

    public Long getInterestCost() {
        return interestCost;
    }

    public void setInterestCost(Long interestCost) {
        this.interestCost = interestCost;
    }

    public Long getSellingExpenses() {
        return sellingExpenses;
    }

    public void setSellingExpenses(Long sellingExpenses) {
        this.sellingExpenses = sellingExpenses;
    }

    public Long getOperatingExpenses() {
        return operatingExpenses;
    }

    public void setOperatingExpenses(Long operatingExpenses) {
        this.operatingExpenses = operatingExpenses;
    }

    public Long getFinancialActivitiesIncome() {
        return financialActivitiesIncome;
    }

    public void setFinancialActivitiesIncome(Long financialActivitiesIncome) {
        this.financialActivitiesIncome = financialActivitiesIncome;
    }

    public Long getOtherIncome() {
        return otherIncome;
    }

    public void setOtherIncome(Long otherIncome) {
        this.otherIncome = otherIncome;
    }

    public Long getEarningsBeforeTaxes() {
        return earningsBeforeTaxes;
    }

    public void setEarningsBeforeTaxes(Long earningsBeforeTaxes) {
        this.earningsBeforeTaxes = earningsBeforeTaxes;
    }

    public Long getCorporateIncomeTax() {
        return corporateIncomeTax;
    }

    public void setCorporateIncomeTax(Long corporateIncomeTax) {
        this.corporateIncomeTax = corporateIncomeTax;
    }

    public Long getProfitAfterTaxes() {
        return profitAfterTaxes;
    }

    public void setProfitAfterTaxes(Long profitAfterTaxes) {
        this.profitAfterTaxes = profitAfterTaxes;
    }

    public Long getNetIncomeAttributableToParent() {
        return netIncomeAttributableToParent;
    }

    public void setNetIncomeAttributableToParent(Long netIncomeAttributableToParent) {
        this.netIncomeAttributableToParent = netIncomeAttributableToParent;
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
