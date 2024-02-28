package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "proprietary_statistic", schema = "vnstock")
@ToString
@Data
public class ProprietaryTradingStatisticEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", length = 20, nullable = false)
    private String symbol;

    @Column(name = "1m_highest_buy_value")
    private Double oneMonthHighestBuyValue;

    @Column(name = "1m_highest_buy_trading_date")
    private LocalDate oneMonthHighestBuyTradingDate;

    @Column(name = "1m_highest_sell_value")
    private Double oneMonthHighestSellValue;

    @Column(name = "1m_highest_sell_trading_date")
    private LocalDate oneMonthHighestSellTradingDate;

    @Column(name = "3m_highest_buy_value")
    private Double threeMonthsHighestBuyValue;

    @Column(name = "3m_highest_buy_trading_date")
    private LocalDate threeMonthsHighestBuyTradingDate;

    @Column(name = "3m_highest_sell_value")
    private Double threeMonthsHighestSellValue;

    @Column(name = "3m_highest_sell_trading_date")
    private LocalDate threeMonthsHighestSellTradingDate;

    @Column(name = "6m_highest_buy_value")
    private Double sixMonthsHighestBuyValue;

    @Column(name = "6m_highest_buy_trading_date")
    private LocalDate sixMonthsHighestBuyTradingDate;

    @Column(name = "6m_highest_sell_value")
    private Double sixMonthsHighestSellValue;

    @Column(name = "6m_highest_sell_trading_date")
    private LocalDate sixMonthsHighestSellTradingDate;

    @Column(name = "12m_highest_buy_value")
    private Double twelveMonthsHighestBuyValue;

    @Column(name = "12m_highest_buy_trading_date")
    private LocalDate twelveMonthsHighestBuyTradingDate;

    @Column(name = "12m_highest_sell_value")
    private Double twelveMonthsHighestSellValue;

    @Column(name = "12m_highest_sell_trading_date")
    private LocalDate twelveMonthsHighestSellTradingDate;

    @Column(name = "highest_buy_value")
    private Double highestBuyValue;

    @Column(name = "highest_buy_trading_date", nullable = false)
    private LocalDate highestBuyTradingDate;

    @Column(name = "highest_sell_value")
    private Double highestSellValue;

    @Column(name = "highest_sell_trading_date", nullable = false)
    private LocalDate highestSellTradingDate;

    // Constructors
    public ProprietaryTradingStatisticEntity() {
        // Default constructor
    }
}
