package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "order_book", schema = "vnstock")
public class OrderBookEntity implements Serializable {

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

    @Column(name = "small_buy_order", nullable = false)
    private Integer smallBuyOrder;

    @Column(name = "small_sell_order", nullable = false)
    private Integer smallSellOrder;

    @Column(name = "medium_buy_order", nullable = false)
    private Integer mediumBuyOrder;

    @Column(name = "medium_sell_order", nullable = false)
    private Integer mediumSellOrder;

    @Column(name = "large_buy_order", nullable = false)
    private Integer largeBuyOrder;

    @Column(name = "large_sell_order", nullable = false)
    private Integer largeSellOrder;

    @Column(name = "trading_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate tradingDate;

    @Column(name = "hash_date", length = 255, nullable = false)
    private String hashDate;

    @Column(name = "atc_volume")
    private Integer atc_volume;

    // Constructors
    public OrderBookEntity() {
        // Default constructor
    }


}
