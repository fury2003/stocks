package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.FloorConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import com.stock.cashflow.persistence.repository.ProprietaryTradingRepository;
import com.stock.cashflow.service.ProprietaryService;
import com.stock.cashflow.utils.DateHelper;
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

    private final ProprietaryTradingRepository proprietaryTradingRepository;

    public ProprietaryServiceImpl(RestTemplate restTemplate, ProprietaryTradingRepository proprietaryTradingRepository){
        this.restTemplate = restTemplate;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
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
                        }else{
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
    public void processSSI() {
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

}
