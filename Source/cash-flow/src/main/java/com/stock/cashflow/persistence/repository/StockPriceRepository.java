package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockPriceRepository extends JpaSpecificationExecutor<StockPriceEntity>, JpaRepository<StockPriceEntity, Long> {

    @Query("select entity from StockPriceEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    StockPriceEntity findStockPriceEntitiesBySymbolAndHashDate(String symbol, String hashDate);


}
