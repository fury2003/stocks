package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProprietaryTradingRepository extends JpaSpecificationExecutor<ProprietaryTradingEntity>, JpaRepository<ProprietaryTradingEntity, Long> {

    @Query("select entity from ProprietaryTradingEntity entity where entity.hashDate =?1")
    ProprietaryTradingEntity findProprietaryTradingEntitiesByHashDate(String hashDate);

    @Query("select SUM(entity.totalNetValue) from ProprietaryTradingEntity entity where entity.tradingDate =?1")
    Double getForeignTotalNetValue(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue > 0")
    Integer getNumberOfBuy(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue < 0")
    Integer getNumberOfSell(LocalDate tradingDate);

    @Query("select COUNT(entity.id) from ProprietaryTradingEntity entity where entity.tradingDate =?1 AND entity.totalNetValue = 0")
    Integer getNumberOfNoChange(LocalDate tradingDate);

}
