package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface ForeignTradingStatisticRepository extends JpaSpecificationExecutor<ForeignTradingStatisticEntity>, JpaRepository<ForeignTradingStatisticEntity, Long> {

    ForeignTradingStatisticEntity findBySymbol(String symbol);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.oneMonthHighestBuyTradingDate=?1 and entity.oneMonthHighestBuyValue > 0 order by entity.oneMonthHighestBuyValue desc")
    List<ForeignTradingStatisticEntity> findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.threeMonthsHighestBuyTradingDate=?1 and entity.threeMonthsHighestBuyValue > 0 order by entity.threeMonthsHighestBuyValue desc")
    List<ForeignTradingStatisticEntity> findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.sixMonthsHighestBuyTradingDate=?1 and entity.sixMonthsHighestBuyValue > 0 order by entity.sixMonthsHighestBuyValue desc")
    List<ForeignTradingStatisticEntity> findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.twelveMonthsHighestBuyTradingDate=?1 and entity.twelveMonthsHighestBuyValue > 0 order by entity.twelveMonthsHighestBuyValue desc")
    List<ForeignTradingStatisticEntity> findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.oneMonthHighestSellTradingDate=?1 and entity.oneMonthHighestSellValue < 0 order by entity.oneMonthHighestSellValue asc")
    List<ForeignTradingStatisticEntity> findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.threeMonthsHighestSellTradingDate=?1 and entity.threeMonthsHighestSellValue < 0 order by entity.threeMonthsHighestSellValue asc")
    List<ForeignTradingStatisticEntity> findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.sixMonthsHighestSellTradingDate=?1 and entity.sixMonthsHighestSellValue < 0 order by entity.sixMonthsHighestSellValue asc")
    List<ForeignTradingStatisticEntity> findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(LocalDate tradingDate);

    @Query("select entity from ForeignTradingStatisticEntity entity where entity.twelveMonthsHighestSellTradingDate=?1 and entity.twelveMonthsHighestSellValue < 0 order by entity.twelveMonthsHighestSellValue asc")
    List<ForeignTradingStatisticEntity> findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(LocalDate tradingDate);



}
