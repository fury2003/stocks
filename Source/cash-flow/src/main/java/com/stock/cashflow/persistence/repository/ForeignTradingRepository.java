package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ForeignTradingRepository extends JpaSpecificationExecutor<ForeignTradingEntity>, JpaRepository<ForeignTradingEntity, Long> {

    @Query("select entity from ForeignTradingEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    ForeignTradingEntity findForeignTradingEntitiesBySymbolAndHashDate(String symbol, String hashDate);

    @Query("select entity from ForeignTradingEntity entity where entity.hashDate =?1")
    ForeignTradingEntity findForeignTradingEntitiesByHashDate(String hashDate);

    @Query("select SUM(entity.totalNetValue) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.symbol NOT IN ('VN30', 'VNINDEX')")
    Double getForeignTotalNetValue(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue > 0")
    Integer getNumberOfBuy(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue < 0")
    Integer getNumberOfSell(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue = 0")
    Integer getNumberOfNoChange(LocalDate tradingDate);
}
