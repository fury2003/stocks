package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.dto.SymbolTotalNetValueDTO;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue > 0 AND entity.totalNetValue > 100000000")
    Integer getNumberOfBuy(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue < 0 AND entity.totalNetValue < 100000000")
    Integer getNumberOfSell(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue = 0")
    Integer getNumberOfNoChange(LocalDate tradingDate);

    @Query("select entity from ProprietaryTradingEntity entity where entity.tradingDate =?1 and entity.symbol not in ('VN30', 'VNINDEX')")
    List<ProprietaryTradingEntity> findByTradingDate(LocalDate tradingDate);

    @Query("select e.totalNetValue from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate between ?2 and ?3 ORDER BY e.totalNetValue DESC LIMIT 1")
    Double getMaxBuyWithDateRange(String symbol, LocalDate start, LocalDate end);

    @Query("select e.totalNetValue from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate between ?2 and ?3 ORDER BY e.totalNetValue ASC LIMIT 1")
    Double getMaxSellWithDateRange(String symbol, LocalDate start, LocalDate end);

    @Query("select e.totalNetValue from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate <= ?2 ORDER BY e.totalNetValue DESC LIMIT 1")
    Double getMaxBuyWithDateRange(String symbol, LocalDate date);

    @Query("select e.totalNetValue from ProprietaryTradingEntity e where e.symbol =?1 and e.tradingDate <= ?2 ORDER BY e.totalNetValue ASC LIMIT 1")
    Double getMaxSellWithDateRange(String symbol, LocalDate date);

    @Query("select e from ProprietaryTradingEntity e where e.tradingDate = ?1 and e.totalNetValue > 0 order by e.totalNetValue desc limit 12")
    List<ProprietaryTradingEntity> getTop12DailyBuy(LocalDate date);

    @Query("select e from ProprietaryTradingEntity e where e.tradingDate = ?1 and e.totalNetValue < 0 order by e.totalNetValue asc limit 12")
    List<ProprietaryTradingEntity> getTop12DailySell(LocalDate date);

    @Query("select new com.stock.cashflow.persistence.dto.SymbolTotalNetValueDTO(e.symbol, SUM(e.totalNetValue)) from ProprietaryTradingEntity e where e.tradingDate BETWEEN ?1 AND ?2 group by e.symbol order by SUM(e.totalNetValue) desc limit 12")
    List<SymbolTotalNetValueDTO> getTop12WeeklyBuy(LocalDate fromDate, LocalDate toDate);

    @Query("select new com.stock.cashflow.persistence.dto.SymbolTotalNetValueDTO(e.symbol, SUM(e.totalNetValue)) from ProprietaryTradingEntity e where e.tradingDate BETWEEN ?1 AND ?2 group by e.symbol order by SUM(e.totalNetValue) asc limit 12")
    List<SymbolTotalNetValueDTO> getTop12WeeklySell(LocalDate fromDate, LocalDate toDate);

    @Query("select entity.totalNetValue from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.symbol = ?2")
    Double getTotalNetValueByTradingDateAndSymbol(LocalDate tradingDate, String symbol);

    @Query("SELECT COUNT(e.id) " +
            "FROM ProprietaryTradingEntity e " +
            "WHERE e.symbol IN :symbols AND e.tradingDate = :tradingDate AND e.totalNetValue > 0")
    Integer getNumberOfBuyInVN30List(@Param("symbols") List<String> symbols,
                                  @Param("tradingDate") LocalDate tradingDate);

    @Query("SELECT COUNT(e.id) " +
            "FROM ProprietaryTradingEntity e " +
            "WHERE e.symbol IN :symbols AND e.tradingDate = :tradingDate AND e.totalNetValue < 0")
    Integer getNumberOfSellInVN30List(@Param("symbols") List<String> symbols,
                                   @Param("tradingDate") LocalDate tradingDate);

}
