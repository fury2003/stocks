package com.stock.cashflow.service.impl;

import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.DerivativesTradingEntity;
import com.stock.cashflow.persistence.repository.DerivativesTradingRepository;
import com.stock.cashflow.service.DerivativesService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@Service
public class DerivativesServiceImpl implements DerivativesService {

    private static final Logger log = LoggerFactory.getLogger(DerivativesServiceImpl.class);

    @Value("${derivatives.market.api.host.baseurl}")
    private String derivativesMarketAPIHostAPIHost;

    @Value("${derivatives.foreign.api.host.baseurl}")
    private String derivativesForeignAPIHostAPIHost;

    @Autowired
    Environment env;

    private final RestTemplate restTemplate;

    private final DerivativesTradingRepository derivativesTradingRepository;


    public DerivativesServiceImpl(RestTemplate restTemplate, DerivativesTradingRepository derivativesTradingRepository){
        this.restTemplate = restTemplate;
        this.derivativesTradingRepository = derivativesTradingRepository;
    }

    @Override
    public void process(String symbol, String startDate, String endDate) {
        log.info("Bat dau sao luu du lieu phai sinh {}", symbol);

        String foreignURL = derivativesForeignAPIHostAPIHost + symbol + "&fromDate=" + startDate + "&toDate=" + endDate;
        String marketURL = derivativesMarketAPIHostAPIHost + symbol + "&fromDate=" + startDate + "&toDate=" + endDate;
        DerivativesForeignDataResponse foreignData = null;
        DerivativesMarketDataResponse marketData = null;

        try{
            ResponseEntity<DerivativesForeignDataResponse> foreignResponse = restTemplate.exchange(foreignURL, HttpMethod.GET, null, DerivativesForeignDataResponse.class);
            ResponseEntity<DerivativesMarketDataResponse> marketResponse = restTemplate.exchange(marketURL, HttpMethod.GET, null, DerivativesMarketDataResponse.class);
            foreignData = foreignResponse.getBody();
            marketData = marketResponse.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
            throw ex;
        }

        if(!Objects.isNull(foreignData.getResult()) && !Objects.isNull(marketData.getResult())){
            DerivativesForeignItems foreignItems = foreignData.getResult();
            DerivativesMarketItems marketItems = marketData.getResult();
            List<DerivativesForeign> foreignList = foreignItems.getItems();
            List<DerivativesMarket> marketList = marketItems.getItems();

            for (int i = 0; i < foreignItems.getItems().size() ; i++) {
                DerivativesTradingEntity entity = new DerivativesTradingEntity();
                entity.setSymbol(symbol);

                double buyVol = foreignList.get(i).getForeignBuyVol();
                double sellVol = foreignList.get(i).getForeignSellVol();
                double buyVal = foreignList.get(i).getForeignBuyValue();
                double sellVal = foreignList.get(i).getForeignSellValue();
                double totalVol = marketList.get(i).getTotalVolume();
                double priceChange = marketList.get(i).getChange();

                entity.setForeignBuyVolume(buyVol);
                entity.setForeignBuyValue(buyVal);
                entity.setForeignSellVolume(sellVol);
                entity.setForeignSellValue(sellVal);
                entity.setTotalVolume(totalVol);

                double percentageChange = marketList.get(i).getChangePercent() * 100;
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);
                entity.setPercentageChange(df.format(percentageChange) + "%");
                entity.setPriceChange(Double.parseDouble(df.format(priceChange)));

                LocalDate tradingDate = marketList.get(i).getTradingTime();
                entity.setTradingDate(tradingDate);

                entity.setHashDate(DigestUtils.sha256Hex(tradingDate + symbol));
                derivativesTradingRepository.save(entity);
                log.info("Luu du lieu phai sinh cho ngay {} thanh cong", tradingDate);
            }
        }else{
            log.info("Khong tim thay du lieu phai sinh tu ngay {} den ngay {}", startDate, endDate);
        }

        log.info("Ket thuc sao luu du lieu phai sinh:" + symbol);
    }

    @Override
    public void updateProprietary(String symbol, String tradingDate, DerivativesProprietaryDTO derivativesProprietaryDTO) {

        String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);

        try{
            DerivativesTradingEntity entity = derivativesTradingRepository.findDerivativesTradingEntitiesByHashDate(hashDate);
            entity.setProprietaryBuyValue(derivativesProprietaryDTO.getProprietaryBuyValue());
            entity.setProprietaryBuyVolume(derivativesProprietaryDTO.getProprietaryBuyVol());
            entity.setProprietarySellValue(derivativesProprietaryDTO.getProprietarySellValue());
            entity.setProprietarySellVolume(derivativesProprietaryDTO.getProprietarySellVol());
            entity.setOpenInterest(derivativesProprietaryDTO.getOpenInterest());
            derivativesTradingRepository.save(entity);
        }catch (Exception ex){
            log.info(ex.getMessage());
            log.error("Loi trong qua trinh cap nhat du lieu phai sinh cua tu doanh");
        }
    }

}
