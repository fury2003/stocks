package com.stock.cashflow.persistence.repository;

import com.stock.cashflow.persistence.entity.OrderBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderBookRepository extends JpaSpecificationExecutor<OrderBookEntity>, JpaRepository<OrderBookEntity, Long> {

    @Query("select entity from OrderBookEntity entity where entity.tradingDate =?1")
    List<OrderBookEntity> getOrderBookEntitiesByTradingDate(LocalDate date);

    @Query("select entity from OrderBookEntity entity where entity.hashDate =?1")
    OrderBookEntity findOrderBookEntitiesByHashDate(String hashDate);

    OrderBookEntity findOrderBookEntitiesBySymbolAndTradingDate(String symbol, LocalDate tradingDate);

    @Query("select count(e) from OrderBookEntity e where e.tradingDate =?1 and e.buyOrder > e.sellOrder and e.mediumBuyOrder > e.mediumSellOrder and e.buyVolume > e.sellVolume")
    Integer strongBuy(LocalDate tradingDate);

    @Query("select count(e) from OrderBookEntity e where e.tradingDate =?1 and e.buyOrder < e.sellOrder and e.mediumBuyOrder < e.mediumSellOrder and e.buyVolume < e.sellVolume")
    Integer strongSell(LocalDate tradingDate);

    @Query("select e.symbol from OrderBookEntity e where e.tradingDate =?1 and e.mediumBuyOrder > e.mediumSellOrder and e.largeBuyOrder > e.largeSellOrder and e.buyVolume > e.sellVolume")
    List<String> getStrongBuy(LocalDate tradingDate);

    @Query("select e.symbol from OrderBookEntity e where e.tradingDate =?1 and e.mediumBuyOrder < e.mediumSellOrder and e.largeBuyOrder < e.largeSellOrder and e.buyVolume < e.sellVolume")
    List<String> getStrongSell(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VCB', 'TCB', 'MBB', 'CTG', 'BID', 'VPB', 'TPB', 'HDB', 'EIB', 'LPB', 'MSB', 'VIB', 'ACB', 'STB', 'OCB', 'SHB', 'NAB')")
    Integer getBankBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VCB', 'TCB', 'MBB', 'CTG', 'BID', 'VPB', 'TPB', 'HDB', 'EIB', 'LPB', 'MSB', 'VIB', 'ACB', 'STB', 'OCB', 'SHB', 'NAB')")
    Integer getBankSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VND', 'SSI', 'BSI', 'FTS', 'VCI', 'HCM', 'VDS', 'ORS', 'SHS', 'VIX', 'CTS', 'MBS', 'AGR', 'TVS')")
    Integer getStockBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VND', 'SSI', 'BSI', 'FTS', 'VCI', 'HCM', 'VDS', 'ORS', 'SHS', 'VIX', 'CTS', 'MBS', 'AGR', 'TVS')")
    Integer getStockSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DXG', 'NLG', 'KDH', 'DIG', 'CEO', 'PDR', 'NVL', 'NTL', 'HDC', 'TCH', 'HQC', 'SJS', 'NHA', 'SCR', 'AGG', 'NBB', 'TDH', 'QCG', 'TIG')")
    Integer getRealEstateBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DXG', 'NLG', 'KDH', 'DIG', 'CEO', 'PDR', 'NVL', 'NTL', 'HDC', 'TCH', 'HQC', 'SJS', 'NHA', 'SCR', 'AGG', 'NBB', 'TDH', 'QCG', 'TIG')")
    Integer getRealEstateSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('KBC', 'IDC', 'SZC', 'PHR', 'SIP', 'NTC', 'VGC', 'GVR', 'BCM', 'IJC', 'LHG', 'DPR', 'ITA')")
    Integer getKCNBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('KBC', 'IDC', 'SZC', 'PHR', 'SIP', 'NTC', 'VGC', 'GVR', 'BCM', 'IJC', 'LHG', 'DPR', 'ITA')")
    Integer getKCNSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VNM', 'DGW', 'MSN', 'MWG', 'PET', 'FRT', 'PNJ', 'SAB')")
    Integer getRetailsBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VNM', 'DGW', 'MSN', 'MWG', 'PET', 'FRT', 'PNJ', 'SAB')")
    Integer getRetailsSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('HPG', 'NLG', 'HSG', 'POM', 'VGS', 'SMC', 'TLH', 'VPG')")
    Integer getSteelBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('HPG', 'NLG', 'HSG', 'POM', 'VGS', 'SMC', 'TLH', 'VPG')")
    Integer getSteelSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('GMD', 'HAH', 'VOS', 'VSC', 'PVT', 'DVP', 'SGP', 'CDN', 'VIP', 'TOS')")
    Integer getLogisticBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('GMD', 'HAH', 'VOS', 'VSC', 'PVT', 'DVP', 'SGP', 'CDN', 'VIP', 'TOS')")
    Integer getLogisticSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('STK', 'MSH', 'VGT', 'TNG', 'TCM', 'ADS', 'GIL', 'EVE', 'M10', 'PPH', 'HTG')")
    Integer getTextileBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('STK', 'MSH', 'VGT', 'TNG', 'TCM', 'ADS', 'GIL', 'EVE', 'M10', 'PPH', 'HTG')")
    Integer getTextileSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('PTB', 'TTF', 'GDT', 'VCS', 'ACG', 'GTA')")
    Integer getWoodBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('PTB', 'TTF', 'GDT', 'VCS', 'ACG', 'GTA')")
    Integer getWoodSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('OIL', 'PLX', 'BSR', 'PVD', 'PVS', 'GAS', 'PVC', 'PVB', 'PSH', 'CNG')")
    Integer getOilBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('OIL', 'PLX', 'BSR', 'PVD', 'PVS', 'GAS', 'PVC', 'PVB', 'PSH', 'CNG')")
    Integer getOilSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VHC', 'ANV', 'IDI', 'FMC', 'ACL', 'CMX', 'MPC')")
    Integer getSeafoodBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('VHC', 'ANV', 'IDI', 'FMC', 'ACL', 'CMX', 'MPC')")
    Integer getSeafoodSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('KSB', 'VLB', 'HT1', 'DHA', 'NNC', 'BCC', 'ACC', 'THG', 'PLC')")
    Integer getMaterialBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('KSB', 'VLB', 'HT1', 'DHA', 'NNC', 'BCC', 'ACC', 'THG', 'PLC')")
    Integer getMaterialSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('HHV', 'C4G', 'CII', 'CTD', 'FCN', 'DTD', 'VCG', 'HBC', 'CTR', 'TV2', 'LCG', 'HUT', 'DPG', 'PC1')")
    Integer getConstructionBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('HHV', 'C4G', 'CII', 'CTD', 'FCN', 'DTD', 'VCG', 'HBC', 'CTR', 'TV2', 'LCG', 'HUT', 'DPG', 'PC1')")
    Integer getConstructionSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('POW', 'GEG', 'REE', 'HDG', 'NT2', 'QTP', 'PPC', 'EVF', 'BCG', 'GEE')")
    Integer getElectricBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('POW', 'GEG', 'REE', 'HDG', 'NT2', 'QTP', 'PPC', 'EVF', 'BCG', 'GEE')")
    Integer getElectricSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DCM', 'DPM', 'BFC', 'LAS', 'DDV', 'CSV', 'DGC', 'PAT')")
    Integer getChemistryBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DCM', 'DPM', 'BFC', 'LAS', 'DDV', 'CSV', 'DGC', 'PAT')")
    Integer getChemistrySellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DBC', 'HAG', 'BAF')")
    Integer getAnimalsBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DBC', 'HAG', 'BAF')")
    Integer getAnimalsSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('BVH', 'BMI', 'MIG', 'PVI', 'BIC', 'VNR')")
    Integer getInsuranceBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('BVH', 'BMI', 'MIG', 'PVI', 'BIC', 'VNR')")
    Integer getInsuranceSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('ACV', 'HVN', 'SAS', 'VJC', 'NCT', 'SGN', 'SCS')")
    Integer getAirlineBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('ACV', 'HVN', 'SAS', 'VJC', 'NCT', 'SGN', 'SCS')")
    Integer getAirlineSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('BMP', 'NTP', 'DNP', 'AAA', 'DAG', 'APH')")
    Integer getPlasticBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('BMP', 'NTP', 'DNP', 'AAA', 'DAG', 'APH')")
    Integer getPlasticSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('FPT', 'CMG', 'ELC', 'FOX', 'VGI', 'ST8')")
    Integer getTechBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('FPT', 'CMG', 'ELC', 'FOX', 'VGI', 'ST8')")
    Integer getTechSellVolume(LocalDate tradingDate);

    @Query("select sum(e.buyVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DHG', 'DBD', 'DBT', 'DMC', 'IMP', 'DCL', 'TRA', 'TNH', 'AMV')")
    Integer getPharmaBuyVolume(LocalDate tradingDate);

    @Query("select sum(e.sellVolume) from OrderBookEntity e where e.tradingDate =?1 and e.symbol IN ('DHG', 'DBD', 'DBT', 'DMC', 'IMP', 'DCL', 'TRA', 'TNH', 'AMV')")
    Integer getPharmaSellVolume(LocalDate tradingDate);


    @Query("SELECT SUM(e.buyVolume) " +
            "FROM OrderBookEntity e " +
            "WHERE e.symbol IN :symbols AND e.tradingDate = :tradingDate")
    Integer getBuyVolume(@Param("symbols") List<String> symbols,
                           @Param("tradingDate") LocalDate tradingDate);

    @Query("SELECT SUM(e.sellVolume) " +
            "FROM OrderBookEntity e " +
            "WHERE e.symbol IN :symbols AND e.tradingDate = :tradingDate")
    Integer getSellVolume(@Param("symbols") List<String> symbols,
                           @Param("tradingDate") LocalDate tradingDate);

}

