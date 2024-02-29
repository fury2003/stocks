package com.stock.cashflow.persistence.repository;


import com.stock.cashflow.persistence.entity.ProprietaryTradingStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProprietaryTradingStatisticRepository extends JpaSpecificationExecutor<ProprietaryTradingStatisticEntity>, JpaRepository<ProprietaryTradingStatisticEntity, Long> {

    ProprietaryTradingStatisticEntity findBySymbol(String symbol);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.oneMonthHighestBuyTradingDate=?1 and entity.oneMonthHighestBuyValue > 0 order by entity.oneMonthHighestBuyValue desc")
    List<ProprietaryTradingStatisticEntity> findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.threeMonthsHighestBuyTradingDate=?1 and entity.threeMonthsHighestBuyValue > 0 order by entity.threeMonthsHighestBuyValue desc")
    List<ProprietaryTradingStatisticEntity> findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.sixMonthsHighestBuyTradingDate=?1 and entity.sixMonthsHighestBuyValue > 0 order by entity.sixMonthsHighestBuyValue desc")
    List<ProprietaryTradingStatisticEntity> findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.twelveMonthsHighestBuyTradingDate=?1 and entity.twelveMonthsHighestBuyValue > 0 order by entity.twelveMonthsHighestBuyValue desc")
    List<ProprietaryTradingStatisticEntity> findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.oneMonthHighestSellTradingDate=?1 and entity.oneMonthHighestSellValue < 0 order by entity.oneMonthHighestSellValue asc")
    List<ProprietaryTradingStatisticEntity> findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.threeMonthsHighestSellTradingDate=?1 and entity.threeMonthsHighestSellValue < 0 order by entity.threeMonthsHighestSellValue asc")
    List<ProprietaryTradingStatisticEntity> findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.sixMonthsHighestSellTradingDate=?1 and entity.sixMonthsHighestSellValue < 0 order by entity.sixMonthsHighestSellValue asc")
    List<ProprietaryTradingStatisticEntity> findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingStatisticEntity entity where entity.twelveMonthsHighestSellTradingDate=?1 and entity.twelveMonthsHighestSellValue < 0 order by entity.twelveMonthsHighestSellValue asc")
    List<ProprietaryTradingStatisticEntity> findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(LocalDate tradingDate);

}
