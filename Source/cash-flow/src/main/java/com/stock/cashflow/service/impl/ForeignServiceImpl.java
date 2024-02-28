package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.ForeignTradingStatisticEntity;
import com.stock.cashflow.persistence.repository.ForeignTradingRepository;
import com.stock.cashflow.persistence.repository.ForeignTradingStatisticRepository;
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

    private final ExcelHelper excelHelper;

    public ForeignServiceImpl(RestTemplate restTemplate, ForeignTradingRepository foreignTradingRepository, ForeignTradingStatisticRepository foreignTradingStatisticRepository, ExcelHelper excelHelper){
        this.restTemplate = restTemplate;
        this.foreignTradingRepository = foreignTradingRepository;
        this.foreignTradingStatisticRepository = foreignTradingStatisticRepository;
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

                            ForeignTradingStatisticEntity statistic = foreignTradingStatisticRepository.findBySymbol(sym.getSymbol());
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
                                    foreignTradingStatisticRepository.save(statistic);
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
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 12 thang");
                                } else if (tnv > statistic.getSixMonthsHighestBuyValue()) {
                                    statistic.setSixMonthsHighestBuyValue(tnv);
                                    statistic.setSixMonthsHighestBuyTradingDate(tradingDate);
                                    statistic.setThreeMonthsHighestBuyValue(tnv);
                                    statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                    statistic.setOneMonthHighestBuyValue(tnv);
                                    statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 6 thang");
                                } else if (tnv > statistic.getThreeMonthsHighestBuyValue()) {
                                    statistic.setThreeMonthsHighestBuyValue(tnv);
                                    statistic.setThreeMonthsHighestBuyTradingDate(tradingDate);
                                    statistic.setOneMonthHighestBuyValue(tnv);
                                    statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 3 thang");
                                } else if (tnv > statistic.getOneMonthHighestBuyValue()) {
                                    statistic.setOneMonthHighestBuyValue(tnv);
                                    statistic.setOneMonthHighestBuyTradingDate(tradingDate);
                                    foreignTradingStatisticRepository.save(statistic);
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
                                    foreignTradingStatisticRepository.save(statistic);
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
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 12 thang");
                                } else if (tnv < statistic.getSixMonthsHighestSellValue()) {
                                    statistic.setSixMonthsHighestSellValue(tnv);
                                    statistic.setSixMonthsHighestSellTradingDate(tradingDate);
                                    statistic.setThreeMonthsHighestSellValue(tnv);
                                    statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                    statistic.setOneMonthHighestSellValue(tnv);
                                    statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 6 thang");
                                } else if (tnv < statistic.getThreeMonthsHighestSellValue()) {
                                    statistic.setThreeMonthsHighestSellValue(tnv);
                                    statistic.setThreeMonthsHighestSellTradingDate(tradingDate);
                                    statistic.setOneMonthHighestSellValue(tnv);
                                    statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 3 thang");
                                } else if (tnv < statistic.getOneMonthHighestSellValue()) {
                                    statistic.setOneMonthHighestSellValue(tnv);
                                    statistic.setOneMonthHighestSellTradingDate(tradingDate);
                                    foreignTradingStatisticRepository.save(statistic);
                                    log.info("Luu thong tin giao dich lon nhat 1 thang");
                                }
                            }
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
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_BUY, "", high1MBuyList, true);
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_BUY, "1M", high1MBuyList, false);
        }

        List<ForeignTradingStatisticEntity> high3MBuyList = foreignTradingStatisticRepository.findByThreeMonthsHighestBuyTradingDateOrderByThreeMonthsHighestBuyValueDesc(date);
        if(high3MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_BUY, "3M", high3MBuyList, false);
        }

        List<ForeignTradingStatisticEntity> high6MBuyList = foreignTradingStatisticRepository.findBySixMonthsHighestBuyTradingDateOrderBySixMonthsHighestBuyValueDesc(date);
        if(high6MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_BUY, "6M", high6MBuyList, false);
        }

        List<ForeignTradingStatisticEntity> high12MBuyList = foreignTradingStatisticRepository.findByTwelveMonthsHighestBuyTradingDateOrderByTwelveMonthsHighestBuyValueDesc(date);
        if(high12MBuyList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_BUY, "12M", high12MBuyList, false);
        }

        List<ForeignTradingStatisticEntity> high1MSellList = foreignTradingStatisticRepository.findByOneMonthHighestSellTradingDateOrderByOneMonthHighestSellValueAsc(date);
        if(high1MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_SELL, "", high1MSellList, true);
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_SELL, "1M", high1MSellList, false);
        }

        List<ForeignTradingStatisticEntity> high3MSellList = foreignTradingStatisticRepository.findByThreeMonthsHighestSellTradingDateOrderByThreeMonthsHighestSellValueAsc(date);
        if(high3MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_SELL, "3M", high3MSellList, false);
        }

        List<ForeignTradingStatisticEntity> high6MSellList = foreignTradingStatisticRepository.findBySixMonthsHighestSellTradingDateOrderBySixMonthsHighestSellValueAsc(date);
        if(high6MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_SELL, "6M", high6MSellList, false);
        }

        List<ForeignTradingStatisticEntity> high12MSellList = foreignTradingStatisticRepository.findByTwelveMonthsHighestSellTradingDateOrderByTwelveMonthsHighestSellValueAsc(date);
        if(high12MSellList.size() > 0){
            excelHelper.writeVolatileForeignTradingToFile(StockConstant.VOLATILE_FOREIGN_SELL, "12M", high12MSellList, false);
        }
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
