package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.DerivativesTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DerivativesTradingRepository extends JpaSpecificationExecutor<DerivativesTradingEntity>, JpaRepository<DerivativesTradingEntity, Long> {

    @Query("select entity from DerivativesTradingEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    DerivativesTradingEntity findDerivativesTradingEntitiesBySymbolAndHashDate(String symbol, String hashDate);

    DerivativesTradingEntity findDerivativesTradingEntitiesByHashDate(String hashDate);
}
