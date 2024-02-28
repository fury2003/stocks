package com.stock.cashflow.persistence.repository;


import com.stock.cashflow.persistence.entity.ProprietaryTradingStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProprietaryTradingStatisticRepository extends JpaSpecificationExecutor<ProprietaryTradingStatisticEntity>, JpaRepository<ProprietaryTradingStatisticEntity, Long> {

    ProprietaryTradingStatisticEntity findBySymbol(String symbol);

    List<ProprietaryTradingStatisticEntity> findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(LocalDate tradingDate);

    List<ProprietaryTradingStatisticEntity> findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(LocalDate tradingDate);

}
