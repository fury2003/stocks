package com.stock.cashflow.service.impl;

import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.DerivativesTradingEntity;
import com.stock.cashflow.persistence.repository.DerivativesTradingRepository;
import com.stock.cashflow.service.DerivativesService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DerivativesServiceImpl implements DerivativesService {

    private static final Logger log = LoggerFactory.getLogger(DerivativesServiceImpl.class);


    @Value("${derivatives.foreign.api.host.baseurl}")
    private String derivativesForeignAPIHost;

    private final DerivativesTradingRepository derivativesTradingRepository;
    private final RestTemplate restTemplate;


    public DerivativesServiceImpl(DerivativesTradingRepository derivativesTradingRepository, RestTemplate restTemplate){
        this.derivativesTradingRepository = derivativesTradingRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Override
    public void processMarketData(List<DerivativesDTO> data) {
        log.info("Bat dau sao luu du lieu phai sinh");

        data.forEach(dto -> {
            DerivativesTradingEntity entity = new DerivativesTradingEntity();
            String symbol = dto.getStockCode();
            entity.setSymbol(symbol);
            entity.setTotalVolume(dto.getTotalVol());
            entity.setPercentageChange(dto.getPerChange().toString());
            entity.setPriceChange(dto.getChange());
            entity.setOpenInterest(dto.getTotalOIVol());

            String tradingDate = dto.getTradingDate();
            String patternString = "\\(([^)]+)\\)";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(tradingDate);
            if (matcher.find()) {
                tradingDate = matcher.group(1);
            }

            Instant instant = Instant.ofEpochMilli(Long.parseLong(tradingDate));
            entity.setTradingDate(instant.atZone(ZoneId.systemDefault()).toLocalDate());

            tradingDate = instant.atZone(ZoneId.systemDefault()).toLocalDate().toString();
            String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
            entity.setHashDate(hashDate);

            derivativesTradingRepository.save(entity);
            log.info("Luu thong tin giao dich phai sinh {} ngay {} thanh cong:", symbol, tradingDate);
        });

        log.info("Ket thuc sao luu du lieu phai sinh:");
    }

    @Override
    public void processForeignData(String symbol, String startDate, String endDate) {
        String url = derivativesForeignAPIHost + symbol +  "&fromDate=" + startDate + "&toDate=" + endDate;

        DerivativesForeignData data = null;
        try{
            ResponseEntity<DerivativesForeignData> response = restTemplate.exchange(url, HttpMethod.GET, null, DerivativesForeignData.class);
            data = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        assert data != null;
        if(data.getResult().getTotalCount() > 0){
            data.getResult().getItems().forEach(item -> {
                String tradingDate = item.getTradingTime().toLocalDate().toString();
                String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
                DerivativesTradingEntity entity = derivativesTradingRepository.findDerivativesTradingEntitiesByHashDate(hashDate);
                Double buyValue = item.getForeignBuyValue() == null ? 0 : item.getForeignBuyValue();
                Double sellValue = item.getForeignSellValue() == null ? 0 : item.getForeignSellValue();

                entity.setForeignBuyVolume(item.getForeignBuyVol());
                entity.setForeignSellVolume(item.getForeignSellVol());
                entity.setForeignBuyValue(buyValue);
                entity.setForeignSellValue(sellValue);

                entity.setForeignNetVolume(item.getForeignBuyVol() - item.getForeignSellVol());
                entity.setForeignNetValue(buyValue - sellValue);
                derivativesTradingRepository.save(entity);
                log.info("Cap nhat thanh cong du lieu phai sinh cua khoi ngoai ngay {} ", tradingDate);
            });
        }
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

            entity.setProprietaryNetVolume(derivativesProprietaryDTO.getProprietaryBuyVol() - derivativesProprietaryDTO.getProprietarySellVol());
            entity.setProprietaryNetValue(derivativesProprietaryDTO.getProprietaryBuyValue() - derivativesProprietaryDTO.getProprietarySellValue());
            derivativesTradingRepository.save(entity);
        }catch (Exception ex){
            log.info(ex.getMessage());
            log.error("Loi trong qua trinh cap nhat du lieu phai sinh cua tu doanh");
            throw new RuntimeException("Loi trong qua trinh cap nhat du lieu phai sinh cua tu doanh");
        }
    }

}
