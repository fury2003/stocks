package com.stock.cashflow.service.impl;

import com.stock.cashflow.dto.*;
import com.stock.cashflow.persistence.entity.IncomeSheetEntity;
import com.stock.cashflow.persistence.repository.IncomeSheetRepository;
import com.stock.cashflow.service.IncomeSheetService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
public class IncomeSheetServiceImpl implements IncomeSheetService {

    private static final Logger log = LoggerFactory.getLogger(IncomeSheetServiceImpl.class);

    private final IncomeSheetRepository incomeSheetRepository;
    private final RestTemplate restTemplate;


    public IncomeSheetServiceImpl(IncomeSheetRepository incomeSheetRepository, RestTemplate restTemplate){
        this.incomeSheetRepository = incomeSheetRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${fa.api.host.baseurl}")
    private String isAPI;

    @Override
    public void crawlData(String ticker, String period, String size) {

        String url = isAPI + "is/" + ticker + "?period=" + period + "&size=" + size;

        IncomeSheetResponse data = null;
        try{
            ResponseEntity<IncomeSheetResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, IncomeSheetResponse.class);
            data = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        if(!Objects.isNull(data)){
            List<IncomeSheetItem> items =  data.getData().getItems();

            items.forEach(item ->{
                IncomeSheetEntity entity = new IncomeSheetEntity();
                entity.setTicker(item.getTicker());
                entity.setNetRevenue(item.getIs4() == null ? item.getIs9() / 1000000 : item.getIs4() / 1000000);
                entity.setGrossProfit(item.getIs2() == null ? 0 : item.getIs2() / 1000000);
                entity.setCogs(item.getIs8() == null ? 0 : item.getIs8() / 1000000);
                entity.setFinanceCharge(item.getIs38() == null ? 0 : item.getIs38() / 1000000);
                entity.setInterestCost(item.getIs51() == null ? 0 : item.getIs51() / 1000000);
                entity.setSellingExpenses(item.getIs52() == null ? 0 : item.getIs52() / 1000000);
                entity.setOperatingExpenses(item.getIs39() / 1000000);
                entity.setFinancialActivitiesIncome(item.getIs37() == null ? 0 : item.getIs37() / 1000000);
                entity.setOtherIncome(item.getIs43() == null ? 0 : item.getIs43() / 1000000);
                entity.setEarningsBeforeTaxes(item.getIs13() / 1000000);
                entity.setCorporateIncomeTax(item.getIs45() / 1000000);
                entity.setProfitAfterTaxes(item.getIs48() / 1000000);
                entity.setNetIncomeAttributableToParent(item.getIs14() / 1000000);
                String quarter = item.getPeriodDateName();
                entity.setQuarter(quarter);
                entity.setHashQuarter(DigestUtils.sha256Hex(quarter + item.getTicker()));

                incomeSheetRepository.save(entity);
                log.info("Luu du lieu income sheet cho quy {} thanh cong ", quarter);
            });
        } else
            log.info("Khong tim thay du lieu income sheet cho cong ty {}", ticker);

    }
}
