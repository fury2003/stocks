package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.FloorConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import com.stock.cashflow.persistence.entity.ProprietaryTradingStatisticEntity;
import com.stock.cashflow.persistence.entity.TradingDateEntity;
import com.stock.cashflow.persistence.repository.ProprietaryTradingRepository;
import com.stock.cashflow.persistence.repository.ProprietaryTradingStatisticRepository;
import com.stock.cashflow.persistence.repository.TradingDateRepository;
import com.stock.cashflow.service.ProprietaryService;
import com.stock.cashflow.utils.DateHelper;
import com.stock.cashflow.utils.ExcelHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProprietaryServiceImpl implements ProprietaryService {

    private static final Logger log = LoggerFactory.getLogger(ProprietaryServiceImpl.class);

    @Value("${proprietary.api.host.ssi}")
    private String proprietarySSIHost;

    @Value("${proprietary.api.host.fireant}")
    private String proprietaryFireantHost;

    @Value("${fireant.token}")
    private String fireantToken;

    private final RestTemplate restTemplate;

    private final ExcelHelper excelHelper;

    private final ProprietaryTradingRepository proprietaryTradingRepository;

    private final ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository;

    private final TradingDateRepository tradingDateRepository;

    public ProprietaryServiceImpl(RestTemplate restTemplate, ProprietaryTradingRepository proprietaryTradingRepository, ExcelHelper excelHelper, ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository, TradingDateRepository tradingDateRepository){
        this.restTemplate = restTemplate;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
        this. proprietaryTradingStatisticRepository = proprietaryTradingStatisticRepository;
        this.excelHelper = excelHelper;
        this.tradingDateRepository = tradingDateRepository;
    }

    public static void main(String[] args) {
        String hashDate = DigestUtils.sha256Hex("2024-06-12" + "CMX");
        log.info(hashDate);
    }

    @Transactional
    @Override
    public void processFireant() {
        String[] floors = FloorConstant.FIREANT_FLOORS;
        for (int i = 0; i < floors.length; i++) {
            List<ProprietaryFireant> filterTrades;
            try{
                String url = proprietaryFireantHost + floors[i];
                HttpHeaders headers = new HttpHeaders();
                headers.set(StockConstant.AUTHORIZATION, fireantToken);
                HttpEntity<String> httpEntity = new HttpEntity<>(headers);
                ResponseEntity<ProprietaryFireant[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ProprietaryFireant[].class);

                List<ProprietaryFireant> allTrades = Arrays.stream(response.getBody()).toList();
                filterTrades = allTrades.stream()
                        .filter(stock -> stock.getSymbol().length() <= 3)
                        .collect(Collectors.toList());

                log.info("Truy xuat du lieu mua ban cua tu doanh tren san {} thanh cong", floors[i]);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu tu Fireant");
                throw new RuntimeException("");
            }

            if(!filterTrades.isEmpty()){
                try{
                    filterTrades.forEach(symbol -> {
                        LocalDate tradingDate = DateHelper.getCurrentLocalDate();
                        String hashDate = DigestUtils.sha256Hex(tradingDate.toString() + symbol.getSymbol());
                        ProprietaryTradingEntity entity = proprietaryTradingRepository.findByTradingDateAndSymbol(tradingDate, symbol.getSymbol());
                        if(Objects.isNull(entity)){
                            ProprietaryTradingEntity newEntity = new ProprietaryTradingEntity();
                            newEntity.setSymbol(symbol.getSymbol());
                            newEntity.setTotalNetValue(symbol.getValue());
                            newEntity.setTradingDate(tradingDate);
                            newEntity.setHashDate(hashDate);
                            proprietaryTradingRepository.save(newEntity);
                            log.info("Luu du lieu mua ban cua tu doanh voi ma {} thanh cong", symbol.getSymbol());
                        } else{
                            log.info("Du lieu mua ban cua tu doanh voi ma {} da ton tai", symbol.getSymbol());
                        }
                    });

                    filterTrades.clear();
                }catch (Exception ex){
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
    }

    @Transactional
    @Override
    public void processAllFloorsFromSSI() {
        String[] floors = FloorConstant.SSI_FLOORS;

        for (int i = 0; i < floors.length; i++) {
            ProprietaryDataResponse proprietaryDataResponse = null;
            try{
                String url = proprietarySSIHost + floors[i];
                ResponseEntity<ProprietaryDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, ProprietaryDataResponse.class);
                proprietaryDataResponse = response.getBody();
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu tu SSI");
                throw ex;
            }

            if(!Objects.isNull(proprietaryDataResponse.getItems())){
                List<ProprietaryItems> items = proprietaryDataResponse.getItems();
                Optional<ProprietaryItems> proprietaryDataOptional = items.stream().findFirst();
                if(proprietaryDataOptional.isPresent()){
                    log.info("Cap nhat du lieu mua ban cua tu doanh tren san {}", floors[i]);
                    String[] symbols = SymbolConstant.ALL_SYMBOLS;
                    ProprietaryToday proprietaryToday = proprietaryDataOptional.get().getToday();
                    List<ProprietaryTrade> proprietaryTrades = proprietaryToday.getBuy();

                    List<ProprietaryTrade> savedData = proprietaryTrades.stream()
                            .filter(proprietaryTrade ->
                                    Arrays.stream(symbols).anyMatch(sym -> sym.equals(proprietaryTrade.getOrganCode())))
                            .collect(Collectors.toList());

                    savedData.forEach(sym -> {
                        log.info("Luu thong tin giao dich tu doanh cua ma {} cho ngay {}", sym.getOrganCode(), sym.getFromDate());
                        ProprietaryTradingEntity entity = sym.convertToEntity();
                        proprietaryTradingRepository.save(entity);
                        log.info("Luu thong tin giao dich tu doanh cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                    });

                    log.info("Ket thuc cap nhat du lieu tu doanh cho sheet");
                }else
                    log.error("Khong tim thay du lieu");
            }else{
                log.error("Khong tim thay thong tin giao dich tu doanh");
            }
        }
    }

    @Override
    public void processSSI(ProprietaryDataResponse proprietaryTextData) {
        List<ProprietaryItems> items = proprietaryTextData.getItems();
        Optional<ProprietaryItems> proprietaryDataOptional = items.stream().findFirst();
        if (proprietaryDataOptional.isPresent()) {
            String[] symbols = SymbolConstant.ALL_SYMBOLS;
            ProprietaryToday proprietaryToday = proprietaryDataOptional.get().getToday();
            List<ProprietaryTrade> proprietaryTrades = proprietaryToday.getBuy();

            List<ProprietaryTrade> savedData = proprietaryTrades.stream()
                    .filter(proprietaryTrade ->
                            Arrays.stream(symbols).anyMatch(sym -> sym.equals(proprietaryTrade.getTicker())))
                    .collect(Collectors.toList());

            savedData.forEach(sym -> {
                log.info("Luu thong tin giao dich tu doanh cua ma {} cho ngay {}", sym.getTicker(), sym.getFromDate());
                Instant instant = sym.getToDate().toInstant();
                LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
//                String hashDate = today.toString() + sym.getOrganCode();
                ProprietaryTradingEntity checkSaved = proprietaryTradingRepository.findByTradingDateAndSymbol(today, sym.getOrganCode());
                if(checkSaved == null){
                    ProprietaryTradingEntity entity = sym.convertToEntity();
                    proprietaryTradingRepository.save(entity);
                    log.info("Luu thong tin giao dich tu doanh cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                }
            });

            log.info("Ket thuc cap nhat du lieu tu doanh cho sheet");
        }
    }

    @Override
    public void processVolatileTrading(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<ProprietaryTradingStatisticEntity> high1MBuyList = proprietaryTradingStatisticRepository.findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(date);
        if(high1MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_BUY, "", high1MBuyList, true, tradingDate);
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_BUY, "1M", high1MBuyList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high3MBuyList = proprietaryTradingStatisticRepository.findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(date);
        if(high3MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_BUY, "3M", high3MBuyList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high6MBuyList = proprietaryTradingStatisticRepository.findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(date);
        if(high6MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_BUY, "6M", high6MBuyList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high12MBuyList = proprietaryTradingStatisticRepository.findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(date);
        if(high12MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_BUY, "12M", high12MBuyList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high1MSellList = proprietaryTradingStatisticRepository.findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(date);
        if(high1MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_SELL, "", high1MSellList, true, tradingDate);
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_SELL, "1M", high1MSellList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high3MSellList = proprietaryTradingStatisticRepository.findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(date);
        if(high3MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_SELL, "3M", high3MSellList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high6MSellList = proprietaryTradingStatisticRepository.findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(date);
        if(high6MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_SELL, "6M", high6MSellList, false, tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high12MSellList = proprietaryTradingStatisticRepository.findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(date);
        if(high12MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.STATISTIC_PROPRIETARY_SELL, "12M", high12MSellList, false, tradingDate);
        }
    }

    @Transactional
    @Override
    public void processStatisticTrading(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<ProprietaryTradingEntity> tradingList = proprietaryTradingRepository.findByTradingDate(date);
        LocalDate yesterday = date.minusDays(1);

        Long id = tradingDateRepository.getIdByTradingDate(date);
//        Long idOf1MonthAgo = tradingDateRepository.getIdOfOneMonthAgo();
        LocalDate dateOf1MonthAgo = tradingDateRepository.getTradingDateById(id-22);
//        Long idOf3MonthAgo = tradingDateRepository.getIdOfThreeMonthAgo();
        LocalDate dateOf3MonthAgo = tradingDateRepository.getTradingDateById(id-66);
//        Long idOf6MonthAgo = tradingDateRepository.getIdOfSixMonthAgo();
        LocalDate dateOf6MonthAgo = tradingDateRepository.getTradingDateById(id-132);
//        Long idOf12MonthAgo = tradingDateRepository.getIdOfOneYearAgo();
//        LocalDate dateOf12MonthAgo = tradingDateRepository.getTradingDateById(idOf12MonthAgo);

        for (ProprietaryTradingEntity entity : tradingList) {
            String symbol = entity.getSymbol();
            log.info("Comparing for {}", symbol);
            Double tnv = entity.getTotalNetValue();

            Double max1Month = proprietaryTradingRepository.getMaxBuyAfterDate(symbol, dateOf1MonthAgo, yesterday);
            if(max1Month == null) {
                log.info("Trading history not found");
                continue;
            }
            // buy side
            if(tnv > max1Month){
                ProprietaryTradingStatisticEntity statisticEntity = proprietaryTradingStatisticRepository.findBySymbol(symbol);
                statisticEntity.setOneMonthHighestBuyValue(tnv);
                statisticEntity.setOneMonthHighestBuyTradingDate(date);
                log.info("Save highest buy of {} in 1 month", symbol);

                Double max3Month = proprietaryTradingRepository.getMaxBuyAfterDate(symbol, dateOf3MonthAgo, yesterday);
                if(tnv > max3Month){
                    statisticEntity.setThreeMonthsHighestBuyValue(tnv);
                    statisticEntity.setThreeMonthsHighestBuyTradingDate(date);
                    log.info("Save highest buy of {} in 3 month", symbol);

                    Double max6Month = proprietaryTradingRepository.getMaxBuyAfterDate(symbol, dateOf6MonthAgo, yesterday);
                    if(tnv > max6Month){
                        statisticEntity.setSixMonthsHighestBuyValue(tnv);
                        statisticEntity.setSixMonthsHighestBuyTradingDate(date);
                        log.info("Save highest buy of {} in 6 month", symbol);
//
//                        Double max12Month = proprietaryTradingRepository.getMaxBuyAfterDate(symbol, dateOf12MonthAgo, yesterday);
//                        if(tnv > max12Month){
//                            statisticEntity.setTwelveMonthsHighestBuyValue(tnv);
//                            statisticEntity.setTwelveMonthsHighestBuyTradingDate(date);
//                            log.info("Save highest buy of {} in 12 month", symbol);
//
                            Double maxTNV = proprietaryTradingRepository.getMaxBuy(symbol, yesterday);
                            if(tnv > maxTNV){
                                statisticEntity.setHighestBuyValue(tnv);
                                statisticEntity.setHighestBuyTradingDate(date);
                                log.info("Save highest buy of {}", symbol);
                            }
                        }
//                    }
                }
                proprietaryTradingStatisticRepository.save(statisticEntity);
            }

            Double min1Month = proprietaryTradingRepository.getMaxSellAfterDate(symbol, dateOf1MonthAgo, yesterday);
            if(min1Month == null) {
                log.info("Trading history not found");
                continue;
            }
            // sell side
            if(tnv < min1Month){
                ProprietaryTradingStatisticEntity statisticEntity = proprietaryTradingStatisticRepository.findBySymbol(symbol);
                statisticEntity.setOneMonthHighestSellValue(tnv);
                statisticEntity.setOneMonthHighestSellTradingDate(date);
                log.info("Save highest sell of {} in 1 month", symbol);

                Double min3Month = proprietaryTradingRepository.getMaxSellAfterDate(symbol, dateOf3MonthAgo, yesterday);
                if(tnv < min3Month){
                    statisticEntity.setThreeMonthsHighestSellValue(tnv);
                    statisticEntity.setThreeMonthsHighestSellTradingDate(date);
                    log.info("Save highest sell of {} in 3 month", symbol);

//                    Double min6Month = proprietaryTradingRepository.getMaxSellAfterDate(symbol, dateOf6MonthAgo, yesterday);
//                    if(tnv < min6Month){
//                        statisticEntity.setSixMonthsHighestSellValue(tnv);
//                        statisticEntity.setSixMonthsHighestSellTradingDate(date);
//                        log.info("Save highest sell of {} in 6 month", symbol);
//
//                        Double min12Month = proprietaryTradingRepository.getMaxSellAfterDate(symbol, dateOf12MonthAgo, yesterday);
//                        if(tnv < min12Month){
//                            statisticEntity.setTwelveMonthsHighestSellValue(tnv);
//                            statisticEntity.setTwelveMonthsHighestSellTradingDate(date);
//                            log.info("Save highest sell of {} in 12 month", symbol);
//
                            Double minTNV = proprietaryTradingRepository.getMaxSell(symbol, yesterday);
                            if(tnv < minTNV){
                                statisticEntity.setHighestSellValue(tnv);
                                statisticEntity.setHighestSellTradingDate(date);
                                log.info("Save highest sell of {}", symbol);
                            }
//                        }
//                    }
                }
                proprietaryTradingStatisticRepository.save(statisticEntity);
            }
        }
        log.info("Complete statistic");
    }

}
