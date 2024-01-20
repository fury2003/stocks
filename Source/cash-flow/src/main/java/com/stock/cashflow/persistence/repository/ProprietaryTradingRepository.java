package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProprietaryTradingRepository extends JpaSpecificationExecutor<ProprietaryTradingEntity>, JpaRepository<ProprietaryTradingEntity, Long> {

    @Query("select entity from ProprietaryTradingEntity entity where entity.hashDate =?1")
    ProprietaryTradingEntity findProprietaryTradingEntitiesByHashDate(String hashDate);


}
