package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "stock_price", schema = "vnstock")
@Data
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

}
