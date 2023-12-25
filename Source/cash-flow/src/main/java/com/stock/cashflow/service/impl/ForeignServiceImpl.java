package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.repository.ForeignTradingRepository;
import com.stock.cashflow.service.ForeignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;

@Service
public class ForeignServiceImpl implements ForeignService {

    private static final Logger log = LoggerFactory.getLogger(ForeignServiceImpl.class);

    @Value("${foreign.api.host.baseurl}")
    private String foreignAPIHost;

    @Value("${fireant.token}")
    private String fireantToken;

    private final Environment env;

    private final RestTemplate restTemplate;

    private final ForeignTradingRepository foreignTradingRepository;

    public ForeignServiceImpl(RestTemplate restTemplate, ForeignTradingRepository foreignTradingRepository, Environment env){
        this.restTemplate = restTemplate;
        this.foreignTradingRepository = foreignTradingRepository;
        this.env = env;
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
                    ForeignTradingEntity entity = sym.convertToEntity();
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
                        ForeignTradingEntity entity = sym.convertToEntity();
                        foreignTradingRepository.save(entity);
                        log.info("Luu thong tin giao dich khoi ngoai cua ma {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                    });
                }catch (Exception ex){
                    log.error("Loi trong qua trinh luu du lieu giao dich cua khoi ngoai");
                    log.info(ex.getMessage());
                    throw ex;
                }
            }else{
                log.error("Khong tim thay thong tin giao dich khoi ngoai");
            }
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
