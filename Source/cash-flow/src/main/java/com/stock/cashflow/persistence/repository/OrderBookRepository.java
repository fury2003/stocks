package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.OrderBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookRepository extends JpaSpecificationExecutor<OrderBookEntity>, JpaRepository<OrderBookEntity, Long> {

    @Query("select entity from OrderBookEntity entity where entity.symbol=?1 and entity.hashDate =?2")
    OrderBookEntity findOrderBookEntitiesBySymbolAndHashDate(String symbol, String hashDate);


}
