package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.IncomeSheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomeSheetRepository extends JpaSpecificationExecutor<IncomeSheetEntity>, JpaRepository<IncomeSheetEntity, Long> {

    @Query("select entity from IncomeSheetEntity entity where entity.hashQuarter =?1")
    IncomeSheetEntity findIncomeSheetEntitiesByHashQuarter(String hashDate);

    List<IncomeSheetEntity> findIncomeSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(String ticker, String fromQuarter, String toQuarter);
}
