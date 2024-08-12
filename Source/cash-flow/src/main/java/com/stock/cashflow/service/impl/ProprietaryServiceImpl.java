package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.FloorConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
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

        log.info(DigestUtils.sha256Hex("2024-07-07" + "PPH"));

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
                            TradingDateEntity today = tradingDateRepository.getTradingDateEntityByTradingDate(tradingDate);
                            LocalDate yesterday = tradingDateRepository.getTradingDateById(today.getId()-1);

                            ProprietaryTradingEntity yesTrading = proprietaryTradingRepository.findByTradingDateAndSymbol(yesterday, symbol.getSymbol());
                            if(!Objects.isNull(yesTrading)){
                                if(yesTrading.getTotalNetValue() == symbol.getValue()){
                                    log.info("Gia tri mua ban cua tu doanh voi ma {} khong thay doi so voi ngay hom qua", symbol.getSymbol());
                                    return;
                                }
                            }

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

    @Transactional
    @Override
    public void processStatisticTrading(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<ProprietaryTradingEntity> tradingList = proprietaryTradingRepository.findByTradingDate(date);
        LocalDate yesterday = date.minusDays(1);

        Long id = tradingDateRepository.getIdByTradingDate(date);
        if(id == null){
            throw new RuntimeException("Trading date not exists");
        }
        LocalDate dateOf1MonthAgo = tradingDateRepository.getTradingDateById(id-22);
        LocalDate dateOf3MonthAgo = tradingDateRepository.getTradingDateById(id-66);
        LocalDate dateOf6MonthAgo = tradingDateRepository.getTradingDateById(id-132);
        LocalDate dateOf12MonthAgo = tradingDateRepository.getTradingDateById(id-264);

        for (ProprietaryTradingEntity entity : tradingList) {
            String symbol = entity.getSymbol();
            log.info("Comparing for {}", symbol);
            Double tnv = entity.getTotalNetValue();
            ProprietaryTradingStatisticEntity statisticEntity = proprietaryTradingStatisticRepository.findBySymbol(symbol);

            // buy side
            if(tnv > 0) {
                boolean isChanged = false;
                log.info("Query biggest buy");
                Double maxTNV = proprietaryTradingRepository.getMaxBuyWithDateRange(symbol, yesterday);
                if(Objects.isNull(maxTNV) || tnv > maxTNV){
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
                    log.info("Query biggest buy within last 12 months from {} to {}", dateOf12MonthAgo, yesterday);
                    Double max12Month = proprietaryTradingRepository.getMaxBuyWithDateRange(symbol, dateOf12MonthAgo, yesterday);
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
                    Double max6Month = proprietaryTradingRepository.getMaxBuyWithDateRange(symbol, dateOf6MonthAgo, yesterday);
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
                    Double max3Month = proprietaryTradingRepository.getMaxBuyWithDateRange(symbol, dateOf3MonthAgo, yesterday);
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
                    Double max1Month = proprietaryTradingRepository.getMaxBuyWithDateRange(symbol, dateOf1MonthAgo, yesterday);
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
                    proprietaryTradingStatisticRepository.save(statisticEntity);
                    log.info("Save top buy change");
                }
            } else if(tnv < 0){
                // sell side
                boolean isChanged = false;
                log.info("Query biggest sell");
                Double minTNV = proprietaryTradingRepository.getMaxSellWithDateRange(symbol, yesterday);
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
                    Double min12Month = proprietaryTradingRepository.getMaxSellWithDateRange(symbol, dateOf12MonthAgo, yesterday);
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
                    Double min6Month = proprietaryTradingRepository.getMaxSellWithDateRange(symbol, dateOf6MonthAgo, yesterday);
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
                    Double min3Month = proprietaryTradingRepository.getMaxSellWithDateRange(symbol, dateOf3MonthAgo, yesterday);
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
                    Double min1Month = proprietaryTradingRepository.getMaxSellWithDateRange(symbol, dateOf1MonthAgo, yesterday);
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
                    proprietaryTradingStatisticRepository.save(statisticEntity);
                    log.info("Save top sell change");
                }
            }
        }
        log.info("Complete statistic");
    }

    @Override
    public void writeTopBuy(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<ProprietaryTradingEntity> dateEntity = proprietaryTradingRepository.findByTradingDate(date);
        if(dateEntity == null) {
            throw new RuntimeException("Khong tim thay ngay giao dich " + tradingDate);
        }

        List<ProprietaryTradingStatisticEntity> high1MBuyList = proprietaryTradingStatisticRepository.findByOneMonthHighestBuyTradingDateOrderByOneMonthHighestBuyValueDesc(date);
        List<ProprietaryTradingStatisticEntity> high3MBuyList = proprietaryTradingStatisticRepository.findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(date);
        List<ProprietaryTradingStatisticEntity> high6MBuyList = proprietaryTradingStatisticRepository.findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(date);
        List<ProprietaryTradingStatisticEntity> high12MBuyList = proprietaryTradingStatisticRepository.findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(date);

        List<ProprietaryTradingStatisticEntity> largestList = getLargestList(high1MBuyList, high3MBuyList, high6MBuyList, high12MBuyList);

        if(!largestList.isEmpty()) {
            List<Double> tnvList = new java.util.ArrayList<>(Collections.emptyList());
            largestList.forEach(entity -> {
                Double tnv = proprietaryTradingRepository.getTotalNetValueByTradingDateAndSymbol(date, entity.getSymbol());
                tnvList.add(tnv);
            });
            excelHelper.writeTotalNetValueToFile(StockConstant.TOP_TD_MUA, "", tnvList, tradingDate);
        }

        if(!high1MBuyList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_MUA, "1M", largestList, high1MBuyList);
            log.info("Cap nhat 1m buy thanh cong");
        }

        if(!high3MBuyList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_MUA, "3M", largestList, high3MBuyList);
            log.info("Cap nhat 3m buy thanh cong");
        }

        if(!high6MBuyList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_MUA, "6M", largestList, high6MBuyList);
            log.info("Cap nhat 6m buy thanh cong");
        }

        if(!high12MBuyList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_MUA, "12M", largestList, high12MBuyList);
            log.info("Cap nhat 12m buy thanh cong");
        }
    }

    @Override
    public void writeTopSell(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);

        List<ProprietaryTradingStatisticEntity> high1MSellList = proprietaryTradingStatisticRepository.findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(date);
        List<ProprietaryTradingStatisticEntity> high3MSellList = proprietaryTradingStatisticRepository.findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(date);
        List<ProprietaryTradingStatisticEntity> high6MSellList = proprietaryTradingStatisticRepository.findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(date);
        List<ProprietaryTradingStatisticEntity> high12MSellList = proprietaryTradingStatisticRepository.findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(date);

        List<ProprietaryTradingStatisticEntity> largestList = getLargestList(high1MSellList, high3MSellList, high6MSellList, high12MSellList);

        if(!largestList.isEmpty()) {
            List<Double> tnvList = new java.util.ArrayList<>(Collections.emptyList());
            largestList.forEach(entity -> {
                Double tnv = proprietaryTradingRepository.getTotalNetValueByTradingDateAndSymbol(date, entity.getSymbol());
                tnvList.add(tnv);
            });
            excelHelper.writeTotalNetValueToFile(StockConstant.TOP_TD_BAN, "", tnvList, tradingDate);
        }

        if(!high1MSellList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_BAN, "1M", largestList, high1MSellList);
            log.info("Cap nhat 1m sell thanh cong");
        }

        if(!high3MSellList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_BAN, "3M", largestList, high3MSellList);
            log.info("Cap nhat 3m sell thanh cong");
        }

        if(!high6MSellList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_BAN, "6M", largestList, high6MSellList);
            log.info("Cap nhat 6m sell thanh cong");
        }

        if(!high12MSellList.isEmpty()){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.TOP_TD_BAN, "12M", largestList, high12MSellList);
            log.info("Cap nhat 12m sell thanh cong");
        }
    }

    @Transactional
    @Override
    public void resetTopBuySell(String date) {
        List<ProprietaryTradingStatisticEntity> allRows = proprietaryTradingStatisticRepository.findAll();

        LocalDate tradingDate = LocalDate.parse(date);
        allRows.forEach(entity -> {
            Double maxBuy = proprietaryTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), tradingDate.minusDays(1));
            if(maxBuy != null){
                entity.setHighestBuyValue(maxBuy);
                entity.setHighestBuyTradingDate(tradingDate);
                proprietaryTradingStatisticRepository.save(entity);
                log.info("Set max buy for {}", entity.getSymbol());
            }
            Double maxSell = proprietaryTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), tradingDate.minusDays(1));

            if(maxSell != null){
                entity.setHighestSellValue(maxSell);
                entity.setHighestSellTradingDate(tradingDate);
                proprietaryTradingStatisticRepository.save(entity);
                log.info("Set max sell for {}", entity.getSymbol());
            }
        });

        LocalDate yesterday = tradingDate.minusDays(1);

        Long id = tradingDateRepository.getIdByTradingDate(tradingDate);
        LocalDate dateOf12MonthAgo = tradingDateRepository.getTradingDateById(id-264);

        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = proprietaryTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy12Month = proprietaryTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf12MonthAgo, yesterday);
                if(maxBuy12Month == null && tnv > 0)
                    maxBuy12Month = tnv;

                if(maxBuy12Month != null && maxBuy12Month > 0){
                    if(tnv >= maxBuy12Month) {
                        entity.setTwelveMonthsHighestBuyValue(tnv);
                        entity.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 12 months buy of {}", entity.getSymbol());
                    }
                }

                Double maxSell12Month = proprietaryTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf12MonthAgo, yesterday);
                if(maxSell12Month == null && tnv < 0)
                    maxSell12Month = tnv;

                if(maxSell12Month != null && maxSell12Month < 0){
                    if(tnv <= maxSell12Month) {
                        entity.setTwelveMonthsHighestSellValue(tnv);
                        entity.setTwelveMonthsHighestSellTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 12 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

        LocalDate dateOf6MonthAgo = tradingDateRepository.getTradingDateById(id-132);
        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = proprietaryTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy6Month = proprietaryTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf6MonthAgo, yesterday);
                if(maxBuy6Month == null && tnv > 0)
                    maxBuy6Month = tnv;

                if(maxBuy6Month != null && maxBuy6Month > 0){
                    if(tnv >= maxBuy6Month) {
                        entity.setSixMonthsHighestBuyValue(tnv);
                        entity.setSixMonthsHighestBuyTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 6 months buy of {}", entity.getSymbol());
                    }
                }
                Double maxSell6Month = proprietaryTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf6MonthAgo, yesterday);
                if(maxSell6Month == null && tnv < 0)
                    maxSell6Month = tnv;

                if(maxSell6Month != null && maxSell6Month < 0){
                    if(tnv <= maxSell6Month) {
                        entity.setSixMonthsHighestSellValue(tnv);
                        entity.setSixMonthsHighestSellTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 6 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

        LocalDate dateOf3MonthAgo = tradingDateRepository.getTradingDateById(id-66);
        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = proprietaryTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy3Month = proprietaryTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf3MonthAgo, yesterday);
                if(maxBuy3Month == null && tnv > 0)
                    maxBuy3Month = tnv;

                if(maxBuy3Month != null && maxBuy3Month > 0){
                    if(tnv >= maxBuy3Month) {
                        entity.setSixMonthsHighestBuyValue(tnv);
                        entity.setSixMonthsHighestBuyTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 3 months buy of {}", entity.getSymbol());
                    }
                }

                Double maxSell3Month = proprietaryTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf3MonthAgo, yesterday);
                if(maxSell3Month == null && tnv < 0)
                    maxSell3Month = tnv;

                if(maxSell3Month != null && maxSell3Month < 0){
                    if(tnv <= maxSell3Month) {
                        entity.setSixMonthsHighestSellValue(tnv);
                        entity.setSixMonthsHighestSellTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 3 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

        LocalDate dateOf1MonthAgo = tradingDateRepository.getTradingDateById(id-22);
        allRows.forEach(entity -> {
            log.info("Query total net value of {}", entity.getSymbol());
            Double tnv = proprietaryTradingRepository.getTotalNetValueByTradingDateAndSymbol(tradingDate, entity.getSymbol());
            if(tnv != null) {
                Double maxBuy1Month = proprietaryTradingRepository.getMaxBuyWithDateRange(entity.getSymbol(), dateOf1MonthAgo, yesterday);
                if(maxBuy1Month == null && tnv > 0)
                    maxBuy1Month = tnv;

                if(maxBuy1Month != null && maxBuy1Month > 0){
                    if(tnv >= maxBuy1Month) {
                        entity.setOneMonthHighestBuyValue(tnv);
                        entity.setOneMonthHighestBuyTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 1 months buy of {}", entity.getSymbol());
                    }
                }

                Double maxSell1Month = proprietaryTradingRepository.getMaxSellWithDateRange(entity.getSymbol(), dateOf1MonthAgo, yesterday);
                if(maxSell1Month == null && tnv < 0 )
                    maxSell1Month = tnv;

                if(maxSell1Month != null && maxSell1Month < 0){
                    if(tnv <= maxSell1Month) {
                        entity.setOneMonthHighestSellValue(tnv);
                        entity.setOneMonthHighestSellTradingDate(tradingDate);
                        proprietaryTradingStatisticRepository.save(entity);
                        log.info("Save top 1 months sell of {}", entity.getSymbol());
                    }
                }
            }
        });

    }

    @Transactional
    @Override
    public void resetStatistic() {
        List<ProprietaryTradingStatisticEntity> allRows = proprietaryTradingStatisticRepository.findAll();
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
            proprietaryTradingStatisticRepository.save(entity);
            log.info("Reset statistic for {}", entity.getSymbol());
        });
        log.info("Reset statistic completed");
    }

    @SafeVarargs
    public static <T> List<ProprietaryTradingStatisticEntity> getLargestList(List<ProprietaryTradingStatisticEntity>... lists) {
        if (lists == null || lists.length == 0) {
            throw new IllegalArgumentException("At least one list must be provided");
        }

        List<ProprietaryTradingStatisticEntity> largestList = Collections.emptyList();
        for (List<ProprietaryTradingStatisticEntity> list : lists) {
            if (list == null) {
                throw new IllegalArgumentException("Lists must not be null");
            }
            if (list.size() > largestList.size()) {
                largestList = list;
            }
        }

        return largestList;
    }


}
