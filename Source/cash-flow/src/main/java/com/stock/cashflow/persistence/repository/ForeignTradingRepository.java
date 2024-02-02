package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ForeignTradingRepository extends JpaSpecificationExecutor<ForeignTradingEntity>, JpaRepository<ForeignTradingEntity, Long> {

    @Query("select entity from ForeignTradingEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    ForeignTradingEntity findForeignTradingEntitiesBySymbolAndHashDate(String symbol, String hashDate);

    @Query("select entity from ForeignTradingEntity entity where entity.hashDate =?1")
    ForeignTradingEntity findForeignTradingEntitiesByHashDate(String hashDate);

}
