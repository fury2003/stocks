package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.dto.ssi.LatestPriceDTO;
import com.stock.cashflow.dto.ssi.LatestPriceItem;
import com.stock.cashflow.dto.ssi.LatestPriceResponse;
import com.stock.cashflow.persistence.entity.StockPriceEntity;
import com.stock.cashflow.persistence.repository.StockPriceRepository;
import com.stock.cashflow.service.StockPriceService;
import com.stock.cashflow.utils.TimeHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StockPriceServiceImpl implements StockPriceService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceServiceImpl.class);

    @Value("${stockPrice.api.host.baseurl}")
    private String stockPriceAPIHost;

    @Value("${stockPrice.api.fireant.url}")
    private String stockPriceFireant;

    @Value("${fireant.token}")
    private String fireantToken;

    private final RestTemplate restTemplate;

    private final StockPriceRepository stockPriceRepository;

    public StockPriceServiceImpl(RestTemplate restTemplate, StockPriceRepository stockPriceRepository) {
        this.restTemplate = restTemplate;
        this.stockPriceRepository = stockPriceRepository;
    }

    @Transactional
    @Override
    public void process(String symbol, String startDate, String endDate) {
        Symbol[] prices = null;
        try{
            prices = getDataFromFireant(symbol, startDate, endDate);
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
            throw ex;
        }

        if(!Objects.isNull(prices)){
            for (int i = 0; i < prices.length - 1; i++) {
                StockPriceEntity entity = prices[i].convertToStockPriceEntity(prices[i + 1].getPriceClose());
                stockPriceRepository.save(entity);
                log.info("Luu lich su gia cua ngay {} {}", entity.getTradingDate(), symbol);
            }
        } else
            log.error("Khong tim thay du lieu thay doi gia cho ma {}", symbol);
    }

    @Transactional
    @Override
    public void processAllSSI(String startDate, String endDate) {
        String[] symbols = SymbolConstant.ALL_SYMBOLS;
        for (int i = 0; i < symbols.length; i++) {
//            StockPriceDataResponse latestPriceDataResponse;
//            try {
//                latestPriceDataResponse = getStockPriceDataResponse(symbols[i], startDate, endDate);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                log.error("Loi trong qua trinh truy xuat du lieu tu SSI");
//                log.info(ex.getMessage());
//                throw ex;
//            }
//
//            if (!Objects.isNull(latestPriceDataResponse.getData())) {
//                List<StockPrice> prices = latestPriceDataResponse.getData();
//                try {
//                    saveStockPrice(prices);
//                } catch (Exception ex) {
//                    log.error("Loi trong qua trinh them du lieu");
//                    log.info(ex.getMessage());
//                    throw ex;
//                }
//            } else
//                log.error("Khong tim thay du lieu thay doi gia cho ma {}", symbols[i]);


            LatestPriceResponse latestPriceResponse;
            try {
                latestPriceResponse = getStockPriceDataResponse(symbols[i]);
            } catch (Exception ex) {
                ex.printStackTrace();
                log.error("Loi trong qua trinh truy xuat du lieu tu SSI");
                log.info(ex.getMessage());
                throw ex;
            }

            if (!latestPriceResponse.getItems().isEmpty()) {

                List<LatestPriceItem> prices = latestPriceResponse.getItems();
                try {
//                    saveStockPrice(prices);
                    if(prices.get(0).getPriceInfo() == null){
                        log.info("Khong tim thay du lieu gia moi nhat cho ma {}", symbols[i]);
                        continue;
                    }
                    saveStockPrice(prices.get(0).getPriceInfo());
                } catch (Exception ex) {
                    log.error("Loi trong qua trinh them du lieu");
                    log.info(ex.getMessage());
                    throw ex;
                }
            } else
                log.error("Khong tim thay du lieu thay doi gia cho ma {}", symbols[i]);

        }
    }

    @Transactional
    @Override
    public void processAllFireant(String startDate, String endDate) {
        String[] symbols = SymbolConstant.SYMBOLS;
        for (int i = 0; i < symbols.length; i++) {
            Symbol[] data = null;
            try{
                data = getDataFromFireant(symbols[i], startDate, endDate);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
                throw ex;
            }

            if(!Objects.isNull(data)){
                log.info("Truy xuat thong tin giao dich khoi ngoai thanh cong");
                try{
                    long count = Arrays.stream(data).count();
                     if(count <= 1){
                        log.info("Ko tim thay du lieu giao dich cua ma {} trong ngay {}", symbols[i], endDate);
                        continue;
                    }
                    Symbol yesterdayData =  Arrays.stream(data).skip(count - 1).findFirst().get();
                    if(!Objects.isNull(yesterdayData)){
                        Optional<Symbol> updatedDate = Arrays.stream(data).findFirst();
                        if(!updatedDate.isPresent())
                            throw new RuntimeException("Khong tim thay data cho ngay " + endDate);

                        StockPriceEntity entity = updatedDate.get().convertToStockPriceEntity(yesterdayData.getPriceClose());
                        stockPriceRepository.save(entity);
                        log.info("Luu thong tin giao dich khoi ngoai cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                    }
                }catch (Exception ex){
                    log.error("Loi trong qua trinh luu du lieu giao dich cua khoi ngoai");
                    ex.printStackTrace();
                    throw ex;
                }
            }else{
                log.error("Khong tim thay thong tin giao dich khoi ngoai");
            }

            TimeHelper.randomSleep();
        }
    }

    private Symbol[] getDataFromFireant(String symbol, String startDate, String endDate){

        String url = String.format(stockPriceFireant, symbol) + "&startDate=" + startDate + "&endDate=" + endDate;
        HttpHeaders headers = new HttpHeaders();
        headers.set(StockConstant.AUTHORIZATION, fireantToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Symbol[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Symbol[].class);
        return response.getBody();

    }

    private LatestPriceResponse getStockPriceDataResponse(String symbol) {
        String url = stockPriceAPIHost + symbol;
        ResponseEntity<LatestPriceResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, LatestPriceResponse.class);
        return response.getBody();

    }

    private void saveStockPrice(LatestPriceDTO price) {
        StockPriceEntity entity = new StockPriceEntity();
        double high = Double.parseDouble(price.getHighestPrice());
        double low = Double.parseDouble(price.getLowestPrice());
        entity.setSymbol(price.getTicker());
        entity.setHighestPrice(high);
        entity.setLowestPrice(low);
        entity.setOpenPrice(Double.parseDouble(price.getOpenPrice()));
        entity.setClosePrice(Double.parseDouble(price.getClosePrice()));
        entity.setPriceChange(Double.parseDouble(price.getPriceChange()));
        entity.setTotalVolume(Double.parseDouble(price.getTotalMatchVolume()));

        double priceRange = ((high - low) / high) * 100;
        double percentageChange = Double.parseDouble(price.getPercentPriceChange()) * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        entity.setPercentageChange(df.format(percentageChange) + "%");
        entity.setPriceRange(df.format(priceRange) + "%");

        LocalDateTime localDateTime = LocalDateTime.parse(price.getTradingDate(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDate tradingDate = localDateTime.toLocalDate();

        entity.setTradingDate(tradingDate);
        entity.setHashDate(DigestUtils.sha256Hex(tradingDate.toString() + price.getTicker()));

        stockPriceRepository.save(entity);
        log.info("Luu du lieu thay doi gia cho ma {} thanh cong", price.getTicker());

    }
}