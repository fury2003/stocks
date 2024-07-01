package com.stock.cashflow.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "foreign_trading", schema = "vnstock")
@Data
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

    @Column(name = "total_net_value")
    private Double totalNetValue;

    @Transient
    private boolean biggest6M;

    @Transient
    private boolean biggestATH;

    @Transient
    private boolean smallest6M;

    @Transient
    private boolean smallestATH;
}
