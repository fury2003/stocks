package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.IntradayOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IntradayOrderRepository extends JpaSpecificationExecutor<IntradayOrderEntity>, JpaRepository<IntradayOrderEntity, Long> {

    @Query("select entity from IntradayOrderEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    IntradayOrderEntity findIntradayOrderEntitiesBySymbolAndHashDate(String symbol, String hashDate);


}
