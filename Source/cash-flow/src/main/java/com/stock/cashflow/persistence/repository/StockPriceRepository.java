package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockPriceRepository extends JpaSpecificationExecutor<StockPriceEntity>, JpaRepository<StockPriceEntity, Long> {

    @Query("select entity.totalVolume from StockPriceEntity entity where entity.symbol=?1 and entity.tradingDate =?2")
    Double findTotalVolumeBySymbolAndTradingDate(String symbol, LocalDate tradingDate);

    @Query("select entity from StockPriceEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    StockPriceEntity findStockPriceEntitiesBySymbolAndHashDate(String symbol, String hashDate);

    @Query("select entity from StockPriceEntity entity where entity.hashDate =?1")
    StockPriceEntity findStockPriceEntitiesByHashDate(String hashDate);

    @Query("SELECT SUM(sp.totalVolume) " +
            "FROM StockPriceEntity sp " +
            "WHERE sp.symbol IN :symbols AND sp.tradingDate = :tradingDate")
    Long getTotalVolumeSum(@Param("symbols") List<String> symbols,
                           @Param("tradingDate") LocalDate tradingDate);

    @Query("select entity.closePrice from StockPriceEntity entity where entity.hashDate=?1")
    Long getClosedPriceByHashDate(String hashDate);

    @Query("SELECT SUM(CAST(entity.percentageChange AS FLOAT)) FROM StockPriceEntity entity WHERE entity.symbol = ?1 AND entity.tradingDate >= ?2 AND entity.tradingDate <= ?3")
    Double getMonthlyPercentageChange(String symbol, LocalDate startDate, LocalDate endDate);

    @Query("select entity from StockPriceEntity entity where entity.tradingDate=?1 and entity.symbol =?2")
    StockPriceEntity getPercentageChangeByTradingDate(LocalDate date, String symbol);

    @Query("select count(entity.id) from StockPriceEntity entity where entity.tradingDate=?1 and entity.highestPrice = entity.closePrice")
    Integer getNumberOfHighEqualClose(LocalDate date);

    @Query("select count(entity.id) from StockPriceEntity entity where entity.tradingDate=?1 and entity.lowestPrice = entity.closePrice")
    Integer getNumberOfLowEqualClose(LocalDate date);

}
