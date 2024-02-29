package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.FloorConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import com.stock.cashflow.persistence.entity.ProprietaryTradingStatisticEntity;
import com.stock.cashflow.persistence.repository.ProprietaryTradingRepository;
import com.stock.cashflow.persistence.repository.ProprietaryTradingStatisticRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public ProprietaryServiceImpl(RestTemplate restTemplate, ProprietaryTradingRepository proprietaryTradingRepository, ExcelHelper excelHelper, ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository){
        this.restTemplate = restTemplate;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
        this. proprietaryTradingStatisticRepository = proprietaryTradingStatisticRepository;
        this.excelHelper = excelHelper;
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
                        ProprietaryTradingEntity entity = proprietaryTradingRepository.findProprietaryTradingEntitiesByHashDate(hashDate);
                        if(Objects.isNull(entity)){
                            ProprietaryTradingEntity newEntity = new ProprietaryTradingEntity();
                            newEntity.setSymbol(symbol.getSymbol());
                            newEntity.setTotalNetValue(symbol.getValue());
                            newEntity.setTradingDate(tradingDate);
                            newEntity.setHashDate(hashDate);
                            proprietaryTradingRepository.save(newEntity);
                            log.info("Luu du lieu mua ban cua tu doanh voi ma {} thanh cong", symbol.getSymbol());

                            ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(symbol.getSymbol());
                            double tnv = symbol.getValue();

                            if(statistic == null){
                                ProprietaryTradingStatisticEntity newEntityStatistic = new ProprietaryTradingStatisticEntity();
                                newEntityStatistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                newEntityStatistic.setOneMonthHighestSellTradingDate(tradingDate);
                                newEntityStatistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                newEntityStatistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                newEntityStatistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                newEntityStatistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                newEntityStatistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                                newEntityStatistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                                newEntityStatistic.setHighestBuyTradingDate(tradingDate);
                                newEntityStatistic.setHighestSellTradingDate(tradingDate);
                                newEntityStatistic.setSymbol(symbol.getSymbol());

                                if (tnv > 0) {
                                    newEntityStatistic.setOneMonthHighestBuyValue(tnv);
                                    newEntityStatistic.setOneMonthHighestSellValue(0.0);
                                    newEntityStatistic.setThreeMonthsHighestBuyValue(tnv);
                                    newEntityStatistic.setThreeMonthsHighestSellValue(0.0);
                                    newEntityStatistic.setSixMonthsHighestBuyValue(tnv);
                                    newEntityStatistic.setSixMonthsHighestSellValue(0.0);
                                    newEntityStatistic.setTwelveMonthsHighestBuyValue(tnv);
                                    newEntityStatistic.setTwelveMonthsHighestSellValue(0.0);
                                    newEntityStatistic.setHighestBuyValue(tnv);
                                    newEntityStatistic.setHighestSellValue(0.0);
                                } else if (tnv < 0){
                                    newEntityStatistic.setOneMonthHighestBuyValue(0.0);
                                    newEntityStatistic.setOneMonthHighestSellValue(tnv);
                                    newEntityStatistic.setThreeMonthsHighestBuyValue(0.0);
                                    newEntityStatistic.setThreeMonthsHighestSellValue(tnv);
                                    newEntityStatistic.setSixMonthsHighestBuyValue(0.0);
                                    newEntityStatistic.setSixMonthsHighestSellValue(tnv);
                                    newEntityStatistic.setTwelveMonthsHighestBuyValue(0.0);
                                    newEntityStatistic.setTwelveMonthsHighestSellValue(tnv);
                                    newEntityStatistic.setHighestBuyValue(0.0);
                                    newEntityStatistic.setHighestSellValue(tnv);
                                }

                                proprietaryTradingStatisticRepository.save(newEntityStatistic);
                                log.info("Luu thong ke giao dich moi {}", symbol.getSymbol());

                            } else {
                                if(tnv > 0){
                                    if (tnv > statistic.getHighestBuyValue()) {
                                        statistic.setHighestBuyValue(tnv);
                                        statistic.setHighestBuyTradingDate(tradingDate);
                                        statistic.setTwelveMonthsHighestBuyValue(tnv);
                                        statistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setSixMonthsHighestBuyValue(tnv);
                                        statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setThreeMonthsHighestBuyValue(tnv);
                                        statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setOneMonthHighestBuyValue(tnv);
                                        statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat");
                                    } else if (tnv > statistic.getTwelveMonthsHighestBuyValue()) {
                                        statistic.setTwelveMonthsHighestBuyValue(tnv);
                                        statistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setSixMonthsHighestBuyValue(tnv);
                                        statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setThreeMonthsHighestBuyValue(tnv);
                                        statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setOneMonthHighestBuyValue(tnv);
                                        statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 12 thang");
                                    } else if (tnv > statistic.getSixMonthsHighestBuyValue()) {
                                        statistic.setSixMonthsHighestBuyValue(tnv);
                                        statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setThreeMonthsHighestBuyValue(tnv);
                                        statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setOneMonthHighestBuyValue(tnv);
                                        statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 6 thang");
                                    } else if (tnv > statistic.getThreeMonthsHighestBuyValue()) {
                                        statistic.setThreeMonthsHighestBuyValue(tnv);
                                        statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                        statistic.setOneMonthHighestBuyValue(tnv);
                                        statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 3 thang");
                                    } else if (tnv > statistic.getOneMonthHighestBuyValue()) {
                                        statistic.setOneMonthHighestBuyValue(tnv);
                                        statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 1 thang");
                                    }

                                } else if (tnv < 0){
                                    if (tnv < statistic.getHighestSellValue()) {
                                        statistic.setHighestSellValue(tnv);
                                        statistic.setHighestSellTradingDate(tradingDate);
                                        statistic.setTwelveMonthsHighestSellValue(tnv);
                                        statistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setSixMonthsHighestSellValue(tnv);
                                        statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setThreeMonthsHighestSellValue(tnv);
                                        statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setOneMonthHighestSellValue(tnv);
                                        statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat");
                                    } else if (tnv < statistic.getTwelveMonthsHighestSellValue()) {
                                        statistic.setTwelveMonthsHighestSellValue(tnv);
                                        statistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setSixMonthsHighestSellValue(tnv);
                                        statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setThreeMonthsHighestSellValue(tnv);
                                        statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setOneMonthHighestSellValue(tnv);
                                        statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 12 thang");
                                    } else if (tnv < statistic.getSixMonthsHighestSellValue()) {
                                        statistic.setSixMonthsHighestSellValue(tnv);
                                        statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setThreeMonthsHighestSellValue(tnv);
                                        statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setOneMonthHighestSellValue(tnv);
                                        statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 6 thang");
                                    } else if (tnv < statistic.getThreeMonthsHighestSellValue()) {
                                        statistic.setThreeMonthsHighestSellValue(tnv);
                                        statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                        statistic.setOneMonthHighestSellValue(tnv);
                                        statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 3 thang");
                                    } else if (tnv < statistic.getOneMonthHighestSellValue()) {
                                        statistic.setOneMonthHighestSellValue(tnv);
                                        statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                        statistic.setSymbol(symbol.getSymbol());
                                        proprietaryTradingStatisticRepository.save(statistic);
                                        log.info("Luu thong tin giao dich lon nhat 1 thang");
                                    }
                                }
                            }
                        } else{
                            log.info("Du lieu mua ban cua tu doanh voi ma {} da ton tai", symbol.getSymbol());
                        }
                    });
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

                        ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(entity.getSymbol());
                        double tnv = entity.getTotalNetValue();
                        LocalDate tradingDate = entity.getTradingDate();
                        if(tnv > 0){
                            if (tnv > statistic.getHighestBuyValue()) {
                                statistic.setHighestBuyValue(tnv);
                                statistic.setHighestBuyTradingDate(tradingDate);
                                statistic.setTwelveMonthsHighestBuyValue(tnv);
                                statistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setSixMonthsHighestBuyValue(tnv);
                                statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setThreeMonthsHighestBuyValue(tnv);
                                statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setOneMonthHighestBuyValue(tnv);
                                statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat");
                            } else if (tnv > statistic.getTwelveMonthsHighestBuyValue()) {
                                statistic.setTwelveMonthsHighestBuyValue(tnv);
                                statistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setSixMonthsHighestBuyValue(tnv);
                                statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setThreeMonthsHighestBuyValue(tnv);
                                statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setOneMonthHighestBuyValue(tnv);
                                statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 12 thang");
                            } else if (tnv > statistic.getSixMonthsHighestBuyValue()) {
                                statistic.setSixMonthsHighestBuyValue(tnv);
                                statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setThreeMonthsHighestBuyValue(tnv);
                                statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setOneMonthHighestBuyValue(tnv);
                                statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 6 thang");
                            } else if (tnv > statistic.getThreeMonthsHighestBuyValue()) {
                                statistic.setThreeMonthsHighestBuyValue(tnv);
                                statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                statistic.setOneMonthHighestBuyValue(tnv);
                                statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 3 thang");
                            } else if (tnv > statistic.getOneMonthHighestBuyValue()) {
                                statistic.setOneMonthHighestBuyValue(tnv);
                                statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 1 thang");
                            }
                        } else if (tnv < 0){
                            if (tnv < statistic.getHighestSellValue()) {
                                statistic.setHighestSellValue(tnv);
                                statistic.setHighestSellTradingDate(tradingDate);
                                statistic.setTwelveMonthsHighestSellValue(tnv);
                                statistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                                statistic.setSixMonthsHighestSellValue(tnv);
                                statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                statistic.setThreeMonthsHighestSellValue(tnv);
                                statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                statistic.setOneMonthHighestSellValue(tnv);
                                statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat");
                            } else if (tnv < statistic.getTwelveMonthsHighestSellValue()) {
                                statistic.setTwelveMonthsHighestSellValue(tnv);
                                statistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                                statistic.setSixMonthsHighestSellValue(tnv);
                                statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                statistic.setThreeMonthsHighestSellValue(tnv);
                                statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                statistic.setOneMonthHighestSellValue(tnv);
                                statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 12 thang");
                            } else if (tnv < statistic.getSixMonthsHighestSellValue()) {
                                statistic.setSixMonthsHighestSellValue(tnv);
                                statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                statistic.setThreeMonthsHighestSellValue(tnv);
                                statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                statistic.setOneMonthHighestSellValue(tnv);
                                statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 6 thang");
                            } else if (tnv < statistic.getThreeMonthsHighestSellValue()) {
                                statistic.setThreeMonthsHighestSellValue(tnv);
                                statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                statistic.setOneMonthHighestSellValue(tnv);
                                statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 3 thang");
                            } else if (tnv < statistic.getOneMonthHighestSellValue()) {
                                statistic.setOneMonthHighestSellValue(tnv);
                                statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                proprietaryTradingStatisticRepository.save(statistic);
                                log.info("Luu thong tin giao dich lon nhat 1 thang");
                            }
                        }
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
                ProprietaryTradingEntity entity = sym.convertToEntity();
                proprietaryTradingRepository.save(entity);
                log.info("Luu thong tin giao dich tu doanh cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());

                ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(entity.getSymbol());

                double tnv = entity.getTotalNetValue();
                LocalDate tradingDate = entity.getTradingDate();

                if(statistic == null){
                    ProprietaryTradingStatisticEntity newEntity = new ProprietaryTradingStatisticEntity();
                    newEntity.setOneMonthHighestBuyTradingDate(tradingDate);
                    newEntity.setOneMonthHighestSellTradingDate(tradingDate);
                    newEntity.setThreeMonthsHighestBuyTradingDate(tradingDate);
                    newEntity.setThreeMonthsHighestSellTradingDate(tradingDate);
                    newEntity.setSixMonthsHighestBuyTradingDate(tradingDate);
                    newEntity.setSixMonthsHighestSellTradingDate(tradingDate);
                    newEntity.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                    newEntity.setTwelveMonthsHighestSellTradingDate(tradingDate);
                    newEntity.setHighestBuyTradingDate(tradingDate);
                    newEntity.setHighestSellTradingDate(tradingDate);
                    newEntity.setSymbol(sym.getTicker());

                    if (tnv > 0) {
                        newEntity.setOneMonthHighestBuyValue(tnv);
                        newEntity.setOneMonthHighestSellValue(0.0);
                        newEntity.setThreeMonthsHighestBuyValue(tnv);
                        newEntity.setThreeMonthsHighestSellValue(0.0);
                        newEntity.setSixMonthsHighestBuyValue(tnv);
                        newEntity.setSixMonthsHighestSellValue(0.0);
                        newEntity.setTwelveMonthsHighestBuyValue(tnv);
                        newEntity.setTwelveMonthsHighestSellValue(0.0);
                        newEntity.setHighestBuyValue(tnv);
                        newEntity.setHighestSellValue(0.0);
                    } else if (tnv < 0){
                        newEntity.setOneMonthHighestBuyValue(0.0);
                        newEntity.setOneMonthHighestSellValue(tnv);
                        newEntity.setThreeMonthsHighestBuyValue(0.0);
                        newEntity.setThreeMonthsHighestSellValue(tnv);
                        newEntity.setSixMonthsHighestBuyValue(0.0);
                        newEntity.setSixMonthsHighestSellValue(tnv);
                        newEntity.setTwelveMonthsHighestBuyValue(0.0);
                        newEntity.setTwelveMonthsHighestSellValue(tnv);
                        newEntity.setHighestBuyValue(0.0);
                        newEntity.setHighestSellValue(tnv);
                    }

                    proprietaryTradingStatisticRepository.save(newEntity);
                    log.info("Luu thong ke giao dich moi {}", sym.getTicker());

                } else {
                    if (tnv > 0) {
                        if (tnv > statistic.getHighestBuyValue()) {
                            statistic.setHighestBuyValue(tnv);
                            statistic.setHighestBuyTradingDate(tradingDate);
                            statistic.setTwelveMonthsHighestBuyValue(tnv);
                            statistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setSixMonthsHighestBuyValue(tnv);
                            statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setThreeMonthsHighestBuyValue(tnv);
                            statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setOneMonthHighestBuyValue(tnv);
                            statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat");
                        } else if (tnv > statistic.getTwelveMonthsHighestBuyValue()) {
                            statistic.setTwelveMonthsHighestBuyValue(tnv);
                            statistic.setTwelveMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setSixMonthsHighestBuyValue(tnv);
                            statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setThreeMonthsHighestBuyValue(tnv);
                            statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setOneMonthHighestBuyValue(tnv);
                            statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 12 thang");
                        } else if (tnv > statistic.getSixMonthsHighestBuyValue()) {
                            statistic.setSixMonthsHighestBuyValue(tnv);
                            statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setThreeMonthsHighestBuyValue(tnv);
                            statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setOneMonthHighestBuyValue(tnv);
                            statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 6 thang");
                        } else if (tnv > statistic.getThreeMonthsHighestBuyValue()) {
                            statistic.setThreeMonthsHighestBuyValue(tnv);
                            statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                            statistic.setOneMonthHighestBuyValue(tnv);
                            statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 3 thang");
                        } else if (tnv > statistic.getOneMonthHighestBuyValue()) {
                            statistic.setOneMonthHighestBuyValue(tnv);
                            statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 1 thang");
                        }
                    } else if (tnv < 0) {
                        if (tnv < statistic.getHighestSellValue()) {
                            statistic.setHighestSellValue(tnv);
                            statistic.setHighestSellTradingDate(tradingDate);
                            statistic.setTwelveMonthsHighestSellValue(tnv);
                            statistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                            statistic.setSixMonthsHighestSellValue(tnv);
                            statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                            statistic.setThreeMonthsHighestSellValue(tnv);
                            statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                            statistic.setOneMonthHighestSellValue(tnv);
                            statistic.setOneMonthHighestSellTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat");
                        } else if (tnv < statistic.getTwelveMonthsHighestSellValue()) {
                            statistic.setTwelveMonthsHighestSellValue(tnv);
                            statistic.setTwelveMonthsHighestSellTradingDate(tradingDate);
                            statistic.setSixMonthsHighestSellValue(tnv);
                            statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                            statistic.setThreeMonthsHighestSellValue(tnv);
                            statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                            statistic.setOneMonthHighestSellValue(tnv);
                            statistic.setOneMonthHighestSellTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 12 thang");
                        } else if (tnv < statistic.getSixMonthsHighestSellValue()) {
                            statistic.setSixMonthsHighestSellValue(tnv);
                            statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                            statistic.setThreeMonthsHighestSellValue(tnv);
                            statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                            statistic.setOneMonthHighestSellValue(tnv);
                            statistic.setOneMonthHighestSellTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 6 thang");
                        } else if (tnv < statistic.getThreeMonthsHighestSellValue()) {
                            statistic.setThreeMonthsHighestSellValue(tnv);
                            statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                            statistic.setOneMonthHighestSellValue(tnv);
                            statistic.setOneMonthHighestSellTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 3 thang");
                        } else if (tnv < statistic.getOneMonthHighestSellValue()) {
                            statistic.setOneMonthHighestSellValue(tnv);
                            statistic.setOneMonthHighestSellTradingDate(tradingDate);
                            statistic.setSymbol(entity.getSymbol());
                            proprietaryTradingStatisticRepository.save(statistic);
                            log.info("Luu thong tin giao dich lon nhat 1 thang");
                        }
                    }
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
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_BUY, "", high1MBuyList, true);
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_BUY, "1M", high1MBuyList, false);
        }

        List<ProprietaryTradingStatisticEntity> high3MBuyList = proprietaryTradingStatisticRepository.findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(date);
        if(high3MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_BUY, "3M", high3MBuyList, false);
        }

        List<ProprietaryTradingStatisticEntity> high6MBuyList = proprietaryTradingStatisticRepository.findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(date);
        if(high6MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_BUY, "6M", high6MBuyList, false);
        }

        List<ProprietaryTradingStatisticEntity> high12MBuyList = proprietaryTradingStatisticRepository.findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(date);
        if(high12MBuyList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_BUY, "12M", high12MBuyList, false);
        }

        List<ProprietaryTradingStatisticEntity> high1MSellList = proprietaryTradingStatisticRepository.findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(date);
        if(high1MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_SELL, "", high1MSellList, true);
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_SELL, "1M", high1MSellList, false);
        }

        List<ProprietaryTradingStatisticEntity> high3MSellList = proprietaryTradingStatisticRepository.findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(date);
        if(high3MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_SELL, "3M", high3MSellList, false);
        }

        List<ProprietaryTradingStatisticEntity> high6MSellList = proprietaryTradingStatisticRepository.findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(date);
        if(high6MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_SELL, "6M", high6MSellList, false);
        }

        List<ProprietaryTradingStatisticEntity> high12MSellList = proprietaryTradingStatisticRepository.findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(date);
        if(high12MSellList.size() > 0){
            excelHelper.writeVolatileProprietaryTradingToFile(StockConstant.VOLATILE_PROPRIETARY_SELL, "12M", high12MSellList, false);
        }
    }


}
