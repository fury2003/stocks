package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface ForeignTradingStatisticRepository extends JpaSpecificationExecutor<ForeignTradingStatisticEntity>, JpaRepository<ForeignTradingStatisticEntity, Long> {

    ForeignTradingStatisticEntity findBySymbol(String symbol);

    List<ForeignTradingStatisticEntity> findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(LocalDate tradingDate);

    List<ForeignTradingStatisticEntity> findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(LocalDate tradingDate);

}
