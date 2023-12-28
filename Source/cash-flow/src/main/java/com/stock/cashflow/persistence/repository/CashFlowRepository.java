package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.CashFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashFlowRepository extends JpaSpecificationExecutor<CashFlowEntity>, JpaRepository<CashFlowEntity, Long> {

    @Query("select entity from CashFlowEntity entity where entity.hashQuarter =?1")
    CashFlowEntity findCashFlowEntitiesByHashQuarter(String hashDate);

    List<CashFlowEntity> findCashFlowEntitiesByTickerAndQuarterBetweenOrderByIdDesc(String ticker, String fromQuarter, String toQuarter);

}
