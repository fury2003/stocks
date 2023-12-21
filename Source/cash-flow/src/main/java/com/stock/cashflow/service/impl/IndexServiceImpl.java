package com.stock.cashflow.service.impl;


import com.stock.cashflow.dto.*;
import com.stock.cashflow.service.IndexService;
import com.stock.cashflow.utils.ExcelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);

    @Autowired
    Environment env;

    private final RestTemplate restTemplate;

    private final ExcelHelper excelHelper;

    public IndexServiceImpl(RestTemplate restTemplate, ExcelHelper excelHelper){
        this.restTemplate = restTemplate;
        this.excelHelper = excelHelper;
    }


    @Override
    public void processProprietaryTradingValue(String index, String quarter) {
//        log.info("Bat dau lay du lieu tu doanh tren san: {}", index);
//        long epochTime = Instant.now().toEpochMilli();
//
//        String url = proprietaryAPIHost + "ComGroupCode=" + index + "&time=" + epochTime;
//
//        ProprietaryDataResponse proprietaryDataResponse = null;
//        try{
//            ResponseEntity<ProprietaryDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, ProprietaryDataResponse.class);
//            proprietaryDataResponse = response.getBody();
//        }catch (Exception ex){
//            throw ex;
//        }
//
//        List<ProprietaryItems> items = proprietaryDataResponse.getItems();
//        Optional<ProprietaryItems> proprietaryDataOptional = items.stream().findFirst();
//        if(proprietaryDataOptional.isPresent()){
//            ProprietaryToday proprietaryToday = proprietaryDataOptional.get().getToday();
//            List<ProprietaryTrade> proprietaryTrades = proprietaryToday.getBuy();
//            List<String> sheetNames = excelHelper.getsheetNames(statisticFilePath);
//
//            List<ProprietaryTrade> filterData = proprietaryTrades.stream()
//                    .filter(proprietaryTrade ->
//                            sheetNames.stream().anyMatch(sheet -> sheet.equals(proprietaryTrade.getOrganCode())))
//                    .collect(Collectors.toList());
//
//            excelHelper.updateProprietaryCell(statisticFilePath, columnIndex, rowIndexStart, rowIndexEnd, filterData);
//            log.info("Ket thuc cap nhat du lieu tu doanh cho sheet");
//        }else
//            log.info("Khong tim thay du lieu");
//
//
//        log.info("Ket thuc lay du lieu tu doanh tren san: {}", index);
    }

}
