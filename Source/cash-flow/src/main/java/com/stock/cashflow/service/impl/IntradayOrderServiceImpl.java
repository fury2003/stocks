package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Intraday;
import com.stock.cashflow.dto.IntradayData;
import com.stock.cashflow.persistence.entity.IntradayOrderEntity;
import com.stock.cashflow.persistence.repository.IntradayOrderRepository;
import com.stock.cashflow.service.IntradayOrderService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class IntradayOrderServiceImpl implements IntradayOrderService {

    private static final Logger log = LoggerFactory.getLogger(IntradayOrderServiceImpl.class);

    @Value("${intraday.api.host.baseurl}")
    private String intradayAPIHost;

//    Environment env;

    private final RestTemplate restTemplate;


    private final IntradayOrderRepository intradayOrderRepository;

    public IntradayOrderServiceImpl(RestTemplate restTemplate, IntradayOrderRepository intradayOrderRepository){
        this.restTemplate = restTemplate;
        this.intradayOrderRepository = intradayOrderRepository;
    }


    @Override
    public void process(String symbol) {
        IntradayData intradayData = null;
        try{
            intradayData = getIntradayDataResponse(symbol);
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        if(!Objects.isNull(intradayData.getData())){
            try{
                saveIntradayOrderData(symbol, intradayData.getData());
            }catch (Exception ex){
                log.error("Loi trong qua trinh them du lieu");
                log.info(ex.getMessage());
                throw ex;
            }

        }else
            log.error("Khong tim thay so lenh giao dich cho ma {}", symbol);

    }

    @Override
    public void processAll() {
        String[] symbols = SymbolConstant.SYMBOLS;

        for (int i = 0; i < symbols.length; i++) {
            IntradayData intradayData = null;
            try{
                intradayData = getIntradayDataResponse(symbols[i]);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu");
                throw ex;
            }

            if(!Objects.isNull(intradayData.getData())){
                try{
                    saveIntradayOrderData(symbols[i], intradayData.getData());
                }catch (Exception ex){
                    log.error("Loi trong qua trinh them du lieu");
                    log.info(ex.getMessage());
                    throw ex;
                }
            }else
                log.error("Khong tim thay so lenh giao dich cho ma {}", symbols[i]);
        }

    }

    private void saveIntradayOrderData(String symbol, List<Intraday> intradayData){
        int buyOrder = 0;
        int sellOrder = 0;
        int buyVolume = 0;
        int sellVolume = 0;

        String floor = intradayData.get(0).getFloor();
        String tradingDate = String.valueOf(intradayData.get(0).getTradingDate());
        if(floor.equals("UPCOM") || floor.equals("HNX")){
            log.info("Khong tim thay thong ke giao dich cho ma chung khoan tren san UPCOM va HNX");
            return;
        }

        for (Intraday item : intradayData) {
            String side = item.getSide();
            if(side.equals("PS")){
                buyOrder++;
                buyVolume += item.getLastVol();
            }else if(side.equals("PB")){
                sellOrder++;
                sellVolume += item.getLastVol();
            }
        }
        log.info("Tong so lenh mua cua ma {}={}", symbol, buyOrder);
        log.info("Tong so lenh ban cua ma {}={}", symbol, sellOrder);
        log.info("Tong khoi luong mua cua ma {}={}", symbol, buyVolume);
        log.info("Tong khoi luong ban cua ma {}={}", symbol, sellVolume);

        IntradayOrderEntity entity = new IntradayOrderEntity();
        entity.setSymbol(symbol);
        entity.setBuyOrder(buyOrder);
        entity.setSellOrder(sellOrder);
        entity.setBuyVolume(buyVolume);
        entity.setSellVolume(sellVolume);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(tradingDate, formatter);
        entity.setTradingDate(date);
        entity.setHashDate(DigestUtils.sha256Hex(tradingDate + symbol));

        intradayOrderRepository.save(entity);
        log.info("Luu thong tin so lenh giao dich cua ma {} thanh cong", entity.getSymbol());

    }

    private IntradayData getIntradayDataResponse(String symbol){
        log.info("Truy xuat danh sach so lenh cho ma {}", symbol);
        String url = intradayAPIHost + symbol;
        ResponseEntity<IntradayData> response = restTemplate.exchange(url, HttpMethod.GET, null, IntradayData.class);
        return response.getBody();

    }
}
