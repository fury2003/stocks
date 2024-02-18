package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.IndexStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface IndexStatisticRepository extends JpaSpecificationExecutor<IndexStatisticEntity>, JpaRepository<IndexStatisticEntity, Long> {

    @Query("select entity.percentageTakenOnIndex from IndexStatisticEntity entity where entity.hashDate =?1")
    String findPercentageTakenOnIndexByHashDate(String hashDate);

}
