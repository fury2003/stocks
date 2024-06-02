package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.TradingDateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TradingDateRepository extends JpaRepository<TradingDateEntity, Long> {

    @Query("select max(id) - 22 from TradingDateEntity")
    Long getIdOfOneMonthAgo();

    @Query("select max(id) - 66 from TradingDateEntity")
    Long getIdOfThreeMonthAgo();

    @Query("select max(id) - 132 from TradingDateEntity")
    Long getIdOfSixMonthAgo();

    @Query("select max(id) - 264 from TradingDateEntity")
    Long getIdOfOneYearAgo();

    @Query("select td.tradingDate from TradingDateEntity td where td.id = ?1")
    LocalDate getTradingDateById(Long id);

    @Query("select td.id from TradingDateEntity td where td.tradingDate = ?1")
    Long getIdByTradingDate(LocalDate date);
}
