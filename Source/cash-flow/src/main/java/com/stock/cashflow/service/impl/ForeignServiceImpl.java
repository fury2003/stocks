package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
import com.stock.cashflow.persistence.repository.ForeignTradingRepository;
import com.stock.cashflow.persistence.repository.ForeignTradingStatisticRepository;
import com.stock.cashflow.persistence.repository.TradingDateRepository;
import com.stock.cashflow.service.ForeignService;
import com.stock.cashflow.utils.ExcelHelper;
import com.stock.cashflow.utils.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ForeignServiceImpl implements ForeignService {

    private static final Logger log = LoggerFactory.getLogger(ForeignServiceImpl.class);

    @Value("${foreign.api.host.baseurl}")
    private String foreignAPIHost;

    @Value("${fireant.token}")
    private String fireantToken;

    private final RestTemplate restTemplate;

    private final ForeignTradingRepository foreignTradingRepository;

    private final ForeignTradingStatisticRepository foreignTradingStatisticRepository;

    private final TradingDateRepository tradingDateRepository;

    private final ExcelHelper excelHelper;

    public ForeignServiceImpl(RestTemplate restTemplate, ForeignTradingRepository foreignTradingRepository, ForeignTradingStatisticRepository foreignTradingStatisticRepository, TradingDateRepository tradingDateRepository, ExcelHelper excelHelper){
        this.restTemplate = restTemplate;
        this.foreignTradingRepository = foreignTradingRepository;
        this.foreignTradingStatisticRepository = foreignTradingStatisticRepository;
        this.tradingDateRepository = tradingDateRepository;
        this.excelHelper = excelHelper;
    }

    @Override
    public void process(String symbol, String startDate, String endDate) {

        Symbol[] data = null;
        try{
            data = getForeignTradingData(symbol, startDate, endDate);
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
            throw ex;
        }

        if(!Objects.isNull(data)){
            log.info("Truy xuat thong tin giao dich khoi ngoai thanh cong");
            try{
                Arrays.stream(data).forEach(sym -> {
                    log.info("Luu thong tin giao dich khoi ngoai cua ma {} cho ngay {}", sym.getSymbol(), sym.getDate());
                    ForeignTradingEntity entity = sym.convertToForeignEntity();
                    foreignTradingRepository.save(entity);
                    log.info("Luu thong tin giao dich khoi ngoai cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                });
            }
            catch(DataIntegrityViolationException ex){
                log.info("Du lieu da ton tai");
                throw ex;
            }
            catch(Exception ex){
                log.error("Loi trong qua trinh luu du lieu giao dich cua khoi ngoai");
                log.info(ex.getMessage());
                throw ex;
            }
        }else{
            log.error("Khong tim thay thong tin giao dich khoi ngoai");
        }
    }

    @Override
    public void processAll(String startDate, String endDate) {

        String[] symbols = SymbolConstant.SYMBOLS;
        for (int i = 0; i < symbols.length; i++) {
            Symbol[] data = null;
            try{
                data = getForeignTradingData(symbols[i], startDate, endDate);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
                throw ex;
            }

            if(!Objects.isNull(data)){
                log.info("Truy xuat thong tin giao dich khoi ngoai thanh cong");
                try{
                    Arrays.stream(data).forEach(sym -> {
                        if(sym.getBuyForeignQuantity() != 0 || sym.getSellForeignQuantity() != 0){
                            ForeignTradingEntity entity = sym.convertToForeignEntity();
                            foreignTradingRepository.save(entity);
                            log.info("Luu thong tin giao dich khoi ngoai cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                        }
                    });
                }catch (Exception ex){
                    log.error("Loi trong qua trinh luu du lieu giao dich cua khoi ngoai");
                    log.info(ex.getMessage());
                    throw ex;
                }
            }else{
                log.error("Khong tim thay thong tin giao dich khoi ngoai");
            }

            TimeHelper.randomSleep();
        }

    }

    @Override
    public void processVolatileTrading(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<ForeignTradingStatisticEntity> high1MBuyList = foreignTradingStatisticRepository.findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(date);
        if(high1MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_BUY, "", high1MBuyList, true, tradingDate);
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_BUY, "1M", high1MBuyList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high3MBuyList = foreignTradingStatisticRepository.findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(date);
        if(high3MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_BUY, "3M", high3MBuyList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high6MBuyList = foreignTradingStatisticRepository.findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(date);
        if(high6MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_BUY, "6M", high6MBuyList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high12MBuyList = foreignTradingStatisticRepository.findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(date);
        if(high12MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_BUY, "12M", high12MBuyList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high1MSellList = foreignTradingStatisticRepository.findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(date);
        if(high1MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_SELL, "", high1MSellList, true, tradingDate);
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_SELL, "1M", high1MSellList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high3MSellList = foreignTradingStatisticRepository.findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(date);
        if(high3MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_SELL, "3M", high3MSellList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high6MSellList = foreignTradingStatisticRepository.findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(date);
        if(high6MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_SELL, "6M", high6MSellList, false, tradingDate);
        }

        List<ForeignTradingStatisticEntity> high12MSellList = foreignTradingStatisticRepository.findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(date);
        if(high12MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.STATISTIC_FOREIGN_SELL, "12M", high12MSellList, false, tradingDate);
        }
    }

    @Transactional
    @Override
    public void processStatisticTrading(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        LocalDate yesterday = date.minusDays(1);
        List<ForeignTradingEntity> tradingList = foreignTradingRepository.findByTradingDate(date);

        Long id = tradingDateRepository.getIdByTradingDate(date);
//        Long idOf1MonthAgo = tradingDateRepository.getIdOfOneMonthAgo();
        LocalDate dateOf1MonthAgo = tradingDateRepository.getTradingDateById(id-22);
//        Long idOf3MonthAgo = tradingDateRepository.getIdOfThreeMonthAgo();
        LocalDate dateOf3MonthAgo = tradingDateRepository.getTradingDateById(id-66);
//        Long idOf6MonthAgo = tradingDateRepository.getIdOfSixMonthAgo();
        LocalDate dateOf6MonthAgo = tradingDateRepository.getTradingDateById(id-132);
//        Long idOf12MonthAgo = tradingDateRepository.getIdOfOneYearAgo();
//        LocalDate dateOf12MonthAgo = tradingDateRepository.getTradingDateById(idOf12MonthAgo);

        for (ForeignTradingEntity entity : tradingList) {
            String symbol = entity.getSymbol();
            log.info("Comparing for {}", symbol);
            Double tnv = entity.getTotalNetValue();

            Double max1Month = foreignTradingRepository.getMaxBuyAfterDate(symbol, dateOf1MonthAgo, yesterday);
            if(max1Month == null) {
                log.info("Trading history not found");
                continue;
            }

            // buy side
            if(tnv > max1Month){
                ForeignTradingStatisticEntity statisticEntity = foreignTradingStatisticRepository.findBySymbol(symbol);
                statisticEntity.setOneMonthHighestBuyValue(tnv);
                statisticEntity.setOneMonthHighestBuyTradingDate(date);
                log.info("Save highest buy of {} in 1 month", symbol);

                Double max3Month = foreignTradingRepository.getMaxBuyAfterDate(symbol, dateOf3MonthAgo, yesterday);
                if(tnv > max3Month){
                    statisticEntity.setThreeMonthsHighestBuyValue(tnv);
                    statisticEntity.setThreeMonthsHighestBuyTradingDate(date);
                    log.info("Save highest buy of {} in 3 month", symbol);

                    Double max6Month = foreignTradingRepository.getMaxBuyAfterDate(symbol, dateOf6MonthAgo, yesterday);
                    if(tnv > max6Month){
                        statisticEntity.setSixMonthsHighestBuyValue(tnv);
                        statisticEntity.setSixMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 6 month", symbol);
//
//                        Double max12Month = foreignTradingRepository.getMaxBuyAfterDate(symbol, dateOf12MonthAgo, yesterday);
//                        if(tnv > max12Month){
//                            statisticEntity.setTwelveMonthsHighestBuyValue(tnv);
//                            statisticEntity.setTwelveMonthsHighestBuyTradingDate(date);
//                            log.info("Save highest buy of {} in 12 month", symbol);
//
                            Double maxTNV = foreignTradingRepository.getMaxBuy(symbol, yesterday);
                            if(tnv > maxTNV){
                                statisticEntity.setHighestBuyValue(tnv);
                                statisticEntity.setHighestBuyTradingDate(date);
                                log.info("Save highest buy of {}", symbol);
                            }
                        }
//                    }
                }
                foreignTradingStatisticRepository.save(statisticEntity);
            }

            Double min1Month = foreignTradingRepository.getMaxSellAfterDate(symbol, dateOf1MonthAgo, yesterday);
            if(min1Month == null) {
                log.info("Trading history not found");
                continue;
            }

            // sell side
            if(tnv < min1Month){
                ForeignTradingStatisticEntity statisticEntity = foreignTradingStatisticRepository.findBySymbol(symbol);
                statisticEntity.setOneMonthHighestSellValue(tnv);
                statisticEntity.setOneMonthHighestSellTradingDate(date);
                log.info("Save highest sell of {} in 1 month", symbol);

                Double min3Month = foreignTradingRepository.getMaxSellAfterDate(symbol, dateOf3MonthAgo, yesterday);
                if(tnv < min3Month){
                    statisticEntity.setThreeMonthsHighestSellValue(tnv);
                    statisticEntity.setThreeMonthsHighestSellTradingDate(date);
                    log.info("Save highest sell of {} in 3 month", symbol);

                    Double min6Month = foreignTradingRepository.getMaxSellAfterDate(symbol, dateOf6MonthAgo, yesterday);
                    if(tnv < min6Month){
                        statisticEntity.setSixMonthsHighestSellValue(tnv);
                        statisticEntity.setSixMonthsHighestSellTradingDate(date);
                        log.info("Save highest sell of {} in 6 month", symbol);
//
//                        Double min12Month = foreignTradingRepository.getMaxSellAfterDate(symbol, dateOf12MonthAgo, yesterday);
//                        if(tnv < min12Month){
//                            statisticEntity.setTwelveMonthsHighestSellValue(tnv);
//                            statisticEntity.setTwelveMonthsHighestSellTradingDate(date);
//                            log.info("Save highest sell of {} in 12 month", symbol);
//
                            Double minTNV = foreignTradingRepository.getMaxSell(symbol, yesterday);
                            if(tnv < minTNV){
                                statisticEntity.setHighestSellValue(tnv);
                                statisticEntity.setHighestSellTradingDate(date);
                                log.info("Save highest sell of {}", symbol);
                            }
                        }
//                    }
                }
                foreignTradingStatisticRepository.save(statisticEntity);
            }
        }
        log.info("Complete statistic");
    }

    private Symbol[] getForeignTradingData(String symbol, String startDate, String endDate){
        String url = foreignAPIHost + "symbols/" + symbol + "/historical-quotes?startDate=" + startDate + "&endDate=" + endDate + "&offset=0&limit=100";
        HttpHeaders headers = new HttpHeaders();
        headers.set(StockConstant.AUTHORIZATION, fireantToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Symbol[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Symbol[].class);
        return response.getBody();

    }
}
