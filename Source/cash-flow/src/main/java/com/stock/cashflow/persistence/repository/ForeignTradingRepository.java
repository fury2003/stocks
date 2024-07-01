package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ForeignTradingRepository extends JpaSpecificationExecutor<ForeignTradingEntity>, JpaRepository<ForeignTradingEntity, Long> {

    @Query("select entity from ForeignTradingEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    ForeignTradingEntity findForeignTradingEntitiesBySymbolAndHashDate(String symbol, String hashDate);

    @Query("select entity from ForeignTradingEntity entity where entity.hashDate =?1")
    ForeignTradingEntity findForeignTradingEntitiesByHashDate(String hashDate);

    @Query("select SUM(entity.totalNetValue) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.symbol NOT IN ('VN30', 'VNINDEX')")
    Double getForeignTotalNetValue(LocalDate tradingDate);

    @Query("select SUM(entity.totalNetValue) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.symbol NOT IN ('VN30', 'VNINDEX', 'VIC', 'VHM', 'VRE')")
    Double getForeignTotalNetValueExcludeVin(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue > 0")
    Integer getNumberOfBuy(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue < 0")
    Integer getNumberOfSell(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ForeignTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue = 0")
    Integer getNumberOfNoChange(LocalDate tradingDate);

    @Query("select entity from ForeignTradingEntity entity where entity.tradingDate =?1 and entity.symbol not in ('VN30', 'VNINDEX')")
    List<ForeignTradingEntity> findByTradingDate(LocalDate tradingDate);

    @Query("select max(e.totalNetValue) from ForeignTradingEntity e where e.symbol =?1 and e.tradingDate between ?2 and ?3")
    Double getMaxBuyAfterDate(String symbol, LocalDate start, LocalDate end);

    @Query("select min(e.totalNetValue) from ForeignTradingEntity e where e.symbol =?1 and e.tradingDate between ?2 and ?3")
    Double getMaxSellAfterDate(String symbol, LocalDate start, LocalDate end);

    @Query("select max(e.totalNetValue) from ForeignTradingEntity e where e.symbol =?1 and e.tradingDate < ?2")
    Double getMaxBuy(String symbol, LocalDate date);

    @Query("select min(e.totalNetValue) from ForeignTradingEntity e where e.symbol =?1 and e.tradingDate < ?2")
    Double getMaxSell(String symbol, LocalDate date);

    @Query("select e from ForeignTradingEntity e where e.tradingDate = ?1 and e.symbol not in ('VN30', 'VNINDEX') order by e.totalNetValue desc limit 10")
    List<ForeignTradingEntity> getTop10ForeignBuy(LocalDate date);

    @Query("select e from ForeignTradingEntity e where e.tradingDate = ?1 and e.symbol not in ('VN30', 'VNINDEX') order by e.totalNetValue asc limit 10")
    List<ForeignTradingEntity> getTop10ForeignSell(LocalDate date);

}
