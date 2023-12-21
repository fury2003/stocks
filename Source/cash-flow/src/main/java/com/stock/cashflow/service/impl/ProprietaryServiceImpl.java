package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.FloorConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import com.stock.cashflow.persistence.repository.ProprietaryTradingRepository;
import com.stock.cashflow.service.ProprietaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProprietaryServiceImpl implements ProprietaryService {

    private static final Logger log = LoggerFactory.getLogger(ProprietaryServiceImpl.class);

    @Value("${proprietary.api.host.baseurl}")
    private String proprietaryAPIHost;

//    Environment env;

    private final RestTemplate restTemplate;

    private final ProprietaryTradingRepository proprietaryTradingRepository;

    public ProprietaryServiceImpl(RestTemplate restTemplate, ProprietaryTradingRepository proprietaryTradingRepository){
        this.restTemplate = restTemplate;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
    }


    @Override
    public void process(String floor) {

        ProprietaryDataResponse proprietaryDataResponse = null;
        try{
            proprietaryDataResponse = getProprietaryTradingDate(floor);
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }
        if(!Objects.isNull(proprietaryDataResponse.getItems())){
            List<ProprietaryItems> items = proprietaryDataResponse.getItems();
            Optional<ProprietaryItems> proprietaryDataOptional = items.stream().findFirst();
            if(proprietaryDataOptional.isPresent()){
                String[] symbols = SymbolConstant.SYMBOLS;
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

    @Override
    public void processAll() {
        String[] floors = FloorConstant.FLOORS;

        for (int i = 0; i < floors.length; i++) {
            ProprietaryDataResponse proprietaryDataResponse = null;
            try{
                proprietaryDataResponse = getProprietaryTradingDate(floors[i]);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
                throw ex;
            }

            if(!Objects.isNull(proprietaryDataResponse.getItems())){
                List<ProprietaryItems> items = proprietaryDataResponse.getItems();
                Optional<ProprietaryItems> proprietaryDataOptional = items.stream().findFirst();
                if(proprietaryDataOptional.isPresent()){
                    String[] symbols = SymbolConstant.SYMBOLS;
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

    private ProprietaryDataResponse getProprietaryTradingDate(String floor){
        long epochTime = Instant.now().toEpochMilli();
        String url = proprietaryAPIHost + "ComGroupCode=" + floor + "&time=" + epochTime;
        ResponseEntity<ProprietaryDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, ProprietaryDataResponse.class);
        return response.getBody();

    }
}
