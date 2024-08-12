package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
import com.stock.cashflow.persistence.entity.TradingDateEntity;
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
import java.util.Collections;
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
    public void writeTopBuy(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        TradingDateEntity dateEntity = tradingDateRepository.getTradingDateEntityByTradingDate(date);
        if(dateEntity == null) {
            throw new RuntimeException("Khong tim thay ngay giao dich " + tradingDate);
        }

        List<ForeignTradingStatisticEntity> high1MBuyList = foreignTradingStatisticRepository.findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(date);
        List<ForeignTradingStatisticEntity> high3MBuyList = foreignTradingStatisticRepository.findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(date);
        List<ForeignTradingStatisticEntity> high6MBuyList = foreignTradingStatisticRepository.findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(date);
        List<ForeignTradingStatisticEntity> high12MBuyList = foreignTradingStatisticRepository.findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(date);

        List<ForeignTradingStatisticEntity> largestList = getLargestList(high1MBuyList, high3MBuyList, high6MBuyList, high12MBuyList);

        if(!largestList.isEmpty()) {
            List<Double> tnvList = new java.util.ArrayList<>(Collections.emptyList());
            largestList.forEach(entity -> {
                Double tnv = foreignTradingRepository.getTotalNetValueByTradingDateAndSymbol(date, entity.getSymbol());
                tnvList.add(tnv);
            });
            excelHelper.writeTotalNetValueToFile(StockConstant.TOP_NN_MUA, "", tnvList, tradingDate);
        }

        if(!high1MBuyList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_MUA, "1M", largestList, high1MBuyList);
            log.info("Cap nhat 1m buy thanh cong");
        }

        if(!high3MBuyList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_MUA, "3M", largestList, high3MBuyList);
            log.info("Cap nhat 3m buy thanh cong");
        }

        if(!high6MBuyList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_MUA, "6M", largestList, high6MBuyList);
            log.info("Cap nhat 6m buy thanh cong");
        }

        if(!high12MBuyList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_MUA, "12M", largestList, high12MBuyList);
            log.info("Cap nhat 12m buy thanh cong");
        }
    }

    @Override
    public void writeTopSell(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<ForeignTradingStatisticEntity> high1MSellList = foreignTradingStatisticRepository.findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(date);
        List<ForeignTradingStatisticEntity> high3MSellList = foreignTradingStatisticRepository.findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(date);
        List<ForeignTradingStatisticEntity> high6MSellList = foreignTradingStatisticRepository.findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(date);
        List<ForeignTradingStatisticEntity> high12MSellList = foreignTradingStatisticRepository.findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(date);

        List<ForeignTradingStatisticEntity> largestList = getLargestList(high1MSellList, high3MSellList, high6MSellList, high12MSellList);

        if(!largestList.isEmpty()) {
            List<Double> tnvList = new java.util.ArrayList<>(Collections.emptyList());
            largestList.forEach(entity -> {
                Double tnv = foreignTradingRepository.getTotalNetValueByTradingDateAndSymbol(date, entity.getSymbol());
                tnvList.add(tnv);
            });
            excelHelper.writeTotalNetValueToFile(StockConstant.TOP_NN_BAN, "", tnvList, tradingDate);
        }

        if(!high1MSellList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_BAN, "1M", largestList, high1MSellList);
            log.info("Cap nhat 1m sell thanh cong");
        }

        if(!high3MSellList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_BAN, "3M", largestList, high3MSellList);
            log.info("Cap nhat 3m sell thanh cong");
        }

        if(!high6MSellList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_BAN, "6M", largestList, high6MSellList);
            log.info("Cap nhat 6m sell thanh cong");
        }

        if(!high12MSellList.isEmpty()){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.TOP_NN_BAN, "12M", largestList, high12MSellList);
            log.info("Cap nhat 12m sell thanh cong");
        }
    }

    @SafeVarargs
    public static <T> List<ForeignTradingStatisticEntity> getLargestList(List<ForeignTradingStatisticEntity>... lists) {
        if (lists == null || lists.length == 0) {
            throw new IllegalArgumentException("At least one list must be provided");
        }

        List<ForeignTradingStatisticEntity> largestList = Collections.emptyList();
        for (List<ForeignTradingStatisticEntity> list : lists) {
            if (list == null) {
                throw new IllegalArgumentException("Lists must not be null");
            }
            if (list.size() > largestList.size()) {
                largestList = list;
            }
        }

        return largestList;
    }

    @Transactional
    @Override
    public void processStatisticTrading(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        LocalDate yesterday = date.minusDays(1);
        List<ForeignTradingEntity> tradingList = foreignTradingRepository.findByTradingDate(date);

        Long id = tradingDateRepository.getIdByTradingDate(date);
        if(id == null){
            throw new RuntimeException("Trading date not exists");
        }
        LocalDate dateOf1MonthAgo = tradingDateRepository.getTradingDateById(id-22);
        LocalDate dateOf3MonthAgo = tradingDateRepository.getTradingDateById(id-66);
        LocalDate dateOf6MonthAgo = tradingDateRepository.getTradingDateById(id-132);
        LocalDate dateOf12MonthAgo = tradingDateRepository.getTradingDateById(id-264);

        for (ForeignTradingEntity entity : tradingList) {
            String symbol = entity.getSymbol();
            log.info("Comparing for {}", symbol);
            Double tnv = entity.getTotalNetValue();
            ForeignTradingStatisticEntity statisticEntity = foreignTradingStatisticRepository.findBySymbol(symbol);

            // buy side
            if(tnv > 0) {
                boolean isChanged = false;
                Double maxTNV = foreignTradingRepository.getMaxBuyWithDateRange(symbol, yesterday);
                log.info("Query biggest buy");
                if(Objects.isNull(maxTNV) || tnv >= maxTNV) {
                    statisticEntity.setHighestBuyValue(tnv);
                    statisticEntity.setHighestBuyTradingDate(date);
                    log.info("Save highest buy of {}", symbol);
                    isChanged = true;
                }

                // no data in 12 months -> use total net value of today
                if(Objects.isNull(dateOf12MonthAgo) && statisticEntity.getTwelveMonthsHighestBuyValue() != null){
                    Double updatedValue = tnv >= statisticEntity.getTwelveMonthsHighestBuyValue() ? tnv : statisticEntity.getTwelveMonthsHighestBuyValue();
                    LocalDate updatedDate = tnv >= statisticEntity.getTwelveMonthsHighestBuyValue() ? date : statisticEntity.getTwelveMonthsHighestBuyTradingDate();
                    statisticEntity.setTwelveMonthsHighestBuyValue(updatedValue);
                    statisticEntity.setTwelveMonthsHighestBuyTradingDate(updatedDate);
                    log.info("Save highest buy of {} in 12 month", symbol);
                    isChanged = true;
                } else {
                    Double max12Month = foreignTradingRepository.getMaxBuyWithDateRange(symbol, dateOf12MonthAgo, yesterday);
                    log.info("Query biggest buy within last 12 months from {} to {}", dateOf12MonthAgo, yesterday);
                    if(max12Month == null) {
                        statisticEntity.setTwelveMonthsHighestBuyValue(tnv);
                        statisticEntity.setTwelveMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 12 month", symbol);
                        isChanged = true;
                    } else if(tnv >= max12Month) {
                        statisticEntity.setTwelveMonthsHighestBuyValue(tnv);
                        statisticEntity.setTwelveMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 12 month", symbol);
                        isChanged = true;
                    }
                }

                // no data in 6 months -> use total net value of today
                if(Objects.isNull(dateOf6MonthAgo) && statisticEntity.getSixMonthsHighestBuyValue() != null){
                    Double updatedValue = tnv >= statisticEntity.getSixMonthsHighestBuyValue() ? tnv : statisticEntity.getSixMonthsHighestBuyValue();
                    LocalDate updatedDate = tnv >= statisticEntity.getSixMonthsHighestBuyValue() ? date : statisticEntity.getSixMonthsHighestBuyTradingDate();
                    statisticEntity.setSixMonthsHighestBuyValue(updatedValue);
                    statisticEntity.setSixMonthsHighestBuyTradingDate(updatedDate);
                    log.info("Save highest buy of {} in 6 month", symbol);
                    isChanged = true;
                } else {
                    Double max6Month = foreignTradingRepository.getMaxBuyWithDateRange(symbol, dateOf6MonthAgo, yesterday);
                    log.info("Query biggest buy within last 6 months from {} to {}", dateOf6MonthAgo, yesterday);

                    if(max6Month == null) {
                        statisticEntity.setSixMonthsHighestBuyValue(tnv);
                        statisticEntity.setSixMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 3 month", symbol);
                        isChanged = true;
                    } else if (tnv >= max6Month) {
                        statisticEntity.setSixMonthsHighestBuyValue(tnv);
                        statisticEntity.setSixMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 6 month", symbol);
                        isChanged = true;
                    }
                }

                // no data in 3 months -> use total net value of today
                if(Objects.isNull(dateOf3MonthAgo) && statisticEntity.getThreeMonthsHighestBuyValue() != null){
                    Double updatedValue = tnv >= statisticEntity.getThreeMonthsHighestBuyValue() ? tnv : statisticEntity.getThreeMonthsHighestBuyValue();
                    LocalDate updatedDate = tnv >= statisticEntity.getThreeMonthsHighestBuyValue() ? date : statisticEntity.getThreeMonthsHighestBuyTradingDate();
                    statisticEntity.setThreeMonthsHighestBuyValue(updatedValue);
                    statisticEntity.setThreeMonthsHighestBuyTradingDate(updatedDate);
                    log.info("Save highest buy of {} in 3 month", symbol);
                    isChanged = true;
                } else {
                    Double max3Month = foreignTradingRepository.getMaxBuyWithDateRange(symbol, dateOf3MonthAgo, yesterday);
                    log.info("Query biggest buy within last 3 months from {} to {}", dateOf3MonthAgo, yesterday);

                    if(max3Month == null) {
                        statisticEntity.setThreeMonthsHighestBuyValue(tnv);
                        statisticEntity.setThreeMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 3 month", symbol);
                        isChanged = true;
                    } else if (tnv >= max3Month){
                        statisticEntity.setThreeMonthsHighestBuyValue(tnv);
                        statisticEntity.setThreeMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 3 month", symbol);
                        isChanged = true;
                    }
                }

                // no data in 1 month -> use total net value of today
                if(Objects.isNull(dateOf1MonthAgo) && statisticEntity.getOneMonthHighestBuyValue() != null){
                    Double updatedValue = tnv >= statisticEntity.getOneMonthHighestBuyValue() ? tnv : statisticEntity.getOneMonthHighestBuyValue();
                    LocalDate updatedDate = tnv >= statisticEntity.getOneMonthHighestBuyValue() ? date : statisticEntity.getOneMonthHighestBuyTradingDate();
                    statisticEntity.setOneMonthHighestBuyValue(updatedValue);
                    statisticEntity.setOneMonthHighestBuyTradingDate(updatedDate);
                    log.info("Save highest buy of {} in 1 month", symbol);
                    isChanged = true;
                } else {
                    Double max1Month = foreignTradingRepository.getMaxBuyWithDateRange(symbol, dateOf1MonthAgo, yesterday);
                    log.info("Query biggest buy within last 1 months from {} to {}", dateOf1MonthAgo, yesterday);
                    if(max1Month == null){
                        statisticEntity.setOneMonthHighestBuyValue(tnv);
                        statisticEntity.setOneMonthHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 1 month", symbol);
                        isChanged = true;
                    } else if(tnv >= max1Month) {
                        statisticEntity.setOneMonthHighestBuyValue(tnv);
                        statisticEntity.setOneMonthHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 1 month", symbol);
                        isChanged = true;
                    }
                }

                if(isChanged){
                    foreignTradingStatisticRepository.save(statisticEntity);
                    log.info("Save top buy change");
                }
            } else if(tnv < 0){
                // sell side
                boolean isChanged = false;
                Double minTNV = foreignTradingRepository.getMaxSellWithDateRange(symbol, yesterday);
                log.info("Query biggest sell");
                if(Objects.isNull(minTNV) || tnv <= minTNV) {
                    statisticEntity.setHighestSellValue(tnv);
                    statisticEntity.setHighestSellTradingDate(date);
                    log.info("Set highest sell of {}", symbol);
                    isChanged = true;
                }

                // no data in 12 months -> use total net value of today
                if(Objects.isNull(dateOf12MonthAgo) && statisticEntity.getTwelveMonthsHighestSellValue() != null){
                    Double updatedValue = tnv <= statisticEntity.getTwelveMonthsHighestSellValue() ? tnv : statisticEntity.getTwelveMonthsHighestSellValue();
                    LocalDate updatedDate = tnv <= statisticEntity.getTwelveMonthsHighestSellValue() ? date : statisticEntity.getTwelveMonthsHighestSellTradingDate();
                    statisticEntity.setTwelveMonthsHighestSellValue(updatedValue);
                    statisticEntity.setTwelveMonthsHighestSellTradingDate(updatedDate);
                    log.info("Set highest sell of {} in 12 month", symbol);
                    isChanged = true;
                } else {
                    Double min12Month = foreignTradingRepository.getMaxSellWithDateRange(symbol, dateOf12MonthAgo, yesterday);
                    log.info("Query biggest sell within last 12 months from {} to {}", dateOf12MonthAgo, yesterday);
                    if(min12Month == null){
                        statisticEntity.setTwelveMonthsHighestSellValue(tnv);
                        statisticEntity.setTwelveMonthsHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 12 month", symbol);
                        isChanged = true;
                    } else if(tnv <= min12Month) {
                        statisticEntity.setTwelveMonthsHighestSellValue(tnv);
                        statisticEntity.setTwelveMonthsHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 12 month", symbol);
                        isChanged = true;
                    }
                }


                // no data in 6 months -> use total net value of today
                if(Objects.isNull(dateOf6MonthAgo) && statisticEntity.getSixMonthsHighestSellValue() != null){
                    Double updatedValue = tnv <= statisticEntity.getSixMonthsHighestSellValue() ? tnv : statisticEntity.getSixMonthsHighestSellValue();
                    LocalDate updatedDate = tnv <= statisticEntity.getSixMonthsHighestSellValue() ? date : statisticEntity.getSixMonthsHighestSellTradingDate();
                    statisticEntity.setSixMonthsHighestSellValue(updatedValue);
                    statisticEntity.setSixMonthsHighestSellTradingDate(updatedDate);
                    log.info("Set highest sell of {} in 6 month", symbol);
                    isChanged = true;
                } else {
                    Double min6Month = foreignTradingRepository.getMaxSellWithDateRange(symbol, dateOf6MonthAgo, yesterday);
                    log.info("Query biggest sell within last 6 months from {} to {}", dateOf6MonthAgo, yesterday);
                    if(min6Month == null){
                        statisticEntity.setSixMonthsHighestSellValue(tnv);
                        statisticEntity.setSixMonthsHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 6 month", symbol);
                        isChanged = true;
                    } else if(tnv <= min6Month) {
                        statisticEntity.setSixMonthsHighestSellValue(tnv);
                        statisticEntity.setSixMonthsHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 6 month", symbol);
                        isChanged = true;
                    }
                }

                // no data in 3 months -> use total net value of today
                if(Objects.isNull(dateOf3MonthAgo) && statisticEntity.getThreeMonthsHighestSellValue() != null) {
                    Double updatedValue = tnv <= statisticEntity.getThreeMonthsHighestSellValue() ? tnv : statisticEntity.getThreeMonthsHighestSellValue();
                    LocalDate updatedDate = tnv <= statisticEntity.getThreeMonthsHighestSellValue() ? date : statisticEntity.getThreeMonthsHighestSellTradingDate();
                    statisticEntity.setThreeMonthsHighestSellValue(updatedValue);
                    statisticEntity.setThreeMonthsHighestSellTradingDate(updatedDate);
                    log.info("Set highest sell of {} in 3 month", symbol);
                    isChanged = true;
                } else {
                    Double min3Month = foreignTradingRepository.getMaxSellWithDateRange(symbol, dateOf3MonthAgo, yesterday);
                    log.info("Query biggest sell within last 3 months from {} to {}", dateOf3MonthAgo, yesterday);
                    if(min3Month == null){
                        statisticEntity.setThreeMonthsHighestSellValue(tnv);
                        statisticEntity.setThreeMonthsHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 3 month", symbol);
                        isChanged = true;
                    } else if(tnv <= min3Month) {
                        statisticEntity.setThreeMonthsHighestSellValue(tnv);
                        statisticEntity.setThreeMonthsHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 3 month", symbol);
                        isChanged = true;
                    }
                }

                // no data in 1 month -> use total net value of today
                if(Objects.isNull(dateOf1MonthAgo) && statisticEntity.getOneMonthHighestSellValue() != null) {
                    Double updatedValue = tnv <= statisticEntity.getOneMonthHighestSellValue() ? tnv : statisticEntity.getOneMonthHighestSellValue();
                    LocalDate updatedDate = tnv <= statisticEntity.getOneMonthHighestSellValue() ? date : statisticEntity.getOneMonthHighestSellTradingDate();
                    statisticEntity.setOneMonthHighestSellValue(updatedValue);
                    statisticEntity.setOneMonthHighestSellTradingDate(updatedDate);
                    log.info("Set highest sell of {} in 1 month", symbol);
                    isChanged = true;
                } else {
                    Double min1Month = foreignTradingRepository.getMaxSellWithDateRange(symbol, dateOf1MonthAgo, yesterday);
                    log.info("Query biggest sell within last 1 months from {} to {}", dateOf1MonthAgo, yesterday);
                    if(min1Month == null){
                        statisticEntity.setOneMonthHighestSellValue(tnv);
                        statisticEntity.setOneMonthHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 1 month", symbol);
                        isChanged = true;
                    } else if(tnv <= min1Month) {
                        statisticEntity.setOneMonthHighestSellValue(tnv);
                        statisticEntity.setOneMonthHighestSellTradingDate(date);
                        log.info("Set highest sell of {} in 1 month", symbol);
                        isChanged = true;
                    }
                }

                if(isChanged){
                    foreignTradingStatisticRepository.save(statisticEntity);
                    log.info("Save top sell change");
                }
            }

        }
        log.info("Complete statistic");
    }

    @Transactional
    @Override
    public void resetStatistic() {
        List<ForeignTradingStatisticEntity> allRows = foreignTradingStatisticRepository.findAll();
        allRows.forEach(entity -> {
            entity.setOneMonthHighestBuyValue(0.0);
            entity.setOneMonthHighestBuyTradingDate(null);
            entity.setThreeMonthsHighestBuyValue(0.0);
            entity.setThreeMonthsHighestBuyTradingDate(null);
            entity.setSixMonthsHighestBuyValue(0.0);
            entity.setSixMonthsHighestBuyTradingDate(null);
            entity.setTwelveMonthsHighestBuyValue(0.0);
            entity.setTwelveMonthsHighestBuyTradingDate(null);
            entity.setHighestBuyValue(0.0);
            entity.setHighestBuyTradingDate(null);
            entity.setOneMonthHighestSellValue(0.0);
            entity.setOneMonthHighestSellTradingDate(null);
            entity.setThreeMonthsHighestSellValue(0.0);
            entity.setThreeMonthsHighestSellTradingDate(null);
            entity.setSixMonthsHighestSellValue(0.0);
            entity.setSixMonthsHighestSellTradingDate(null);
            entity.setTwelveMonthsHighestSellValue(0.0);
            entity.setTwelveMonthsHighestSellTradingDate(null);
            entity.setHighestSellValue(0.0);
            entity.setHighestSellTradingDate(null);
            foreignTradingStatisticRepository.save(entity);
            log.info("Reset statistic for {}", entity.getSymbol());
        });
        log.info("Reset statistic completed");
    }

    @Transactional
    @Override
    public void resetTopBuySell(String date) {
        List<ForeignTradingStatisticEntity> allRows = foreignTradingStatisticRepository.findAll();

        LocalDate tradingDate = LocalDate.parse(date);
        allRows.forEach(entity -> {
            Double maxBuy = foreignTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), tradingDate);
            Double maxSell = foreignTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), tradingDate);
            if(maxBuy != null){
                entity.setHighestBuyValue(maxBuy);
                entity.setHighestBuyTradingDate(tradingDate);
                foreignTradingStatisticRepository.save(entity);
                log.info("Set max buy for {}", entity.getSymbol());
            }

            if(maxSell != null){
                entity.setHighestSellValue(maxSell);
                entity.setHighestSellTradingDate(tradingDate);
                foreignTradingStatisticRepository.save(entity);
                log.info("Set max sell for {}", entity.getSymbol());
            }
        });

        LocalDate yesterday = tradingDate.minusDays(1);

        Long id = tradingDateRepository.getIdByTradingDate(tradingDate);
        LocalDate dateOf12MonthAgo = tradingDateRepository.getTradingDateById(id-264);

        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = foreignTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy12Month = foreignTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf12MonthAgo, yesterday);
                if(maxBuy12Month == null && tnv > 0)
                    maxBuy12Month = tnv;

                if(maxBuy12Month != null && maxBuy12Month > 0){
                    if(tnv >= maxBuy12Month) {
                        entity.setTwelveMonthsHighestBuyValue(tnv);
                        entity.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 12 months buy of {}", entity.getSymbol());
                    }
                }

                Double maxSell12Month = foreignTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf12MonthAgo, yesterday);
                if(maxSell12Month == null && tnv < 0)
                    maxSell12Month = tnv;

                if(maxSell12Month != null && maxSell12Month < 0){
                    if(tnv <= maxSell12Month) {
                        entity.setTwelveMonthsHighestSellValue(tnv);
                        entity.setTwelveMonthsHighestSellTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 12 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

        LocalDate dateOf6MonthAgo = tradingDateRepository.getTradingDateById(id-132);
        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = foreignTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy6Month = foreignTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf6MonthAgo, yesterday);
                if(maxBuy6Month == null && tnv > 0)
                    maxBuy6Month = tnv;

                if(maxBuy6Month != null && maxBuy6Month > 0){
                    if(tnv >= maxBuy6Month) {
                        entity.setSixMonthsHighestBuyValue(tnv);
                        entity.setSixMonthsHighestBuyTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 6 months buy of {}", entity.getSymbol());
                    }
                }
                Double maxSell6Month = foreignTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf6MonthAgo, yesterday);
                if(maxSell6Month == null && tnv < 0)
                    maxSell6Month = tnv;

                if(maxSell6Month != null && maxSell6Month < 0){
                    if(tnv <= maxSell6Month) {
                        entity.setSixMonthsHighestSellValue(tnv);
                        entity.setSixMonthsHighestSellTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 6 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

        LocalDate dateOf3MonthAgo = tradingDateRepository.getTradingDateById(id-66);
        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = foreignTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy3Month = foreignTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf3MonthAgo, yesterday);
                if(maxBuy3Month == null && tnv > 0)
                    maxBuy3Month = tnv;

                if(maxBuy3Month != null && maxBuy3Month > 0){
                    if(tnv >= maxBuy3Month) {
                        entity.setThreeMonthsHighestBuyValue(tnv);
                        entity.setThreeMonthsHighestBuyTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 3 months buy of {}", entity.getSymbol());
                    }
                }

                Double maxSell3Month = foreignTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf3MonthAgo, yesterday);
                if(maxSell3Month == null && tnv < 0)
                    maxSell3Month = tnv;

                if(maxSell3Month != null && maxSell3Month < 0){
                    if(tnv <= maxSell3Month) {
                        entity.setThreeMonthsHighestSellValue(tnv);
                        entity.setThreeMonthsHighestSellTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 3 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

        LocalDate dateOf1MonthAgo = tradingDateRepository.getTradingDateById(id-22);
        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = foreignTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy1Month = foreignTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf1MonthAgo, yesterday);
                if(maxBuy1Month == null && tnv > 0)
                    maxBuy1Month = tnv;

                if(maxBuy1Month != null && maxBuy1Month > 0){
                    if(tnv >= maxBuy1Month) {
                        entity.setOneMonthHighestBuyValue(tnv);
                        entity.setOneMonthHighestBuyTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 1 months buy of {}", entity.getSymbol());
                    }
                }

                Double maxSell1Month = foreignTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf1MonthAgo, yesterday);
                if(maxSell1Month == null && tnv < 0 )
                    maxSell1Month = tnv;

                if(maxSell1Month != null && maxSell1Month < 0){
                    if(tnv <= maxSell1Month) {
                        entity.setOneMonthHighestSellValue(tnv);
                        entity.setOneMonthHighestSellTradingDate(tradingDate);
                        foreignTradingStatisticRepository.save(entity);
                        log.info("Save top 1 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });
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
