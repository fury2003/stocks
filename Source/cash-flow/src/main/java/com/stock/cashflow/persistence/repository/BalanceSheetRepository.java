package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.BalanceSheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceSheetRepository extends JpaSpecificationExecutor<BalanceSheetEntity>, JpaRepository<BalanceSheetEntity, Long> {

    @Query("select entity from BalanceSheetEntity entity where entity.hashQuarter =?1")
    BalanceSheetEntity findBalanceSheetEntitiesByHashQuarter(String hashDate);

    List<BalanceSheetEntity> findBalanceSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(String ticker, String fromQuarter, String toQuarter);

}
