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

    @Query("select entity.totalVolume from StockPriceEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    Double findTotalVolumeBySymbolAndHashDate(String symbol, String hashDate);

    @Query("select entity from StockPriceEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    StockPriceEntity findStockPriceEntitiesBySymbolAndHashDate(String symbol, String hashDate);

    @Query("select entity from StockPriceEntity entity where entity.hashDate =?1")
    StockPriceEntity findStockPriceEntitiesByHashDate(String hashDate);

    @Query("SELECT SUM(sp.totalVolume) " +
            "FROM StockPriceEntity sp " +
            "WHERE sp.symbol IN :symbols AND sp.tradingDate = :tradingDate")
    Long getTotalVolumeSum(@Param("symbols") List<String> symbols,
                           @Param("tradingDate") LocalDate tradingDate);

}
