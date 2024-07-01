package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProprietaryTradingRepository extends JpaSpecificationExecutor<ProprietaryTradingEntity>, JpaRepository<ProprietaryTradingEntity, Long> {

    @Query("select e from ProprietaryTradingEntity e where e.tradingDate =?1 and e.symbol=?2")
    ProprietaryTradingEntity findByTradingDateAndSymbol(LocalDate tradingDate, String symbol);

    @Query("select e from ProprietaryTradingEntity e where e.hashDate =?1")
    ProprietaryTradingEntity findProprietaryTradingEntitiesByHashDate(String hashDate);

    @Query("select SUM(entity.totalNetValue) from ProprietaryTradingEntity entity where entity.tradingDate =?1")
    Double getForeignTotalNetValue(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue > 0")
    Integer getNumberOfBuy(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue < 0")
    Integer getNumberOfSell(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue = 0")
    Integer getNumberOfNoChange(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingEntity entity where entity.tradingDate =?1 and entity.symbol not in ('VN30', 'VNINDEX')")
    List<ProprietaryTradingEntity> findByTradingDate(LocalDate tradingDate);

    @Query("select max(e.totalNetValue) from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate between ?2 and ?3")
    Double getMaxBuyAfterDate(String symbol, LocalDate start, LocalDate end);

    @Query("select min(e.totalNetValue) from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate between ?2 and ?3")
    Double getMaxSellAfterDate(String symbol, LocalDate start, LocalDate end);

    @Query("select max(e.totalNetValue) from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate <= ?2")
    Double getMaxBuy(String symbol, LocalDate date);

    @Query("select min(e.totalNetValue) from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate <= ?2")
    Double getMaxSell(String symbol, LocalDate date);

    @Query("select e from ProprietaryTradingEntity e where e.tradingDate = ?1 order by e.totalNetValue desc limit 10")
    List<ProprietaryTradingEntity> getTop10ProprietaryBuy(LocalDate date);

    @Query("select e from ProprietaryTradingEntity e where e.tradingDate = ?1 order by e.totalNetValue asc limit 10")
    List<ProprietaryTradingEntity> getTop10ProprietarySell(LocalDate date);

}
