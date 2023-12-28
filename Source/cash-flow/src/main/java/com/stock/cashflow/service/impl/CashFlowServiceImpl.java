package com.stock.cashflow.service.impl;

import com.stock.cashflow.dto.CashFlowItem;
import com.stock.cashflow.dto.CashFlowResponse;
import com.stock.cashflow.persistence.entity.CashFlowEntity;
import com.stock.cashflow.persistence.repository.CashFlowRepository;
import com.stock.cashflow.service.CashFlowService;
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
public class CashFlowServiceImpl implements CashFlowService {

    private static final Logger log = LoggerFactory.getLogger(CashFlowServiceImpl.class);

    private final CashFlowRepository cashFlowRepository;
    private final RestTemplate restTemplate;


    public CashFlowServiceImpl(CashFlowRepository cashFlowRepository, RestTemplate restTemplate){
        this.cashFlowRepository = cashFlowRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${fa.api.host.baseurl}")
    private String bsAPI;

    @Override
    public void crawlData(String ticker, String period, String size) {

        String url = bsAPI + "cf/" + ticker + "?period=" + period + "&size=" + size;

        CashFlowResponse data = null;
        try{
            ResponseEntity<CashFlowResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, CashFlowResponse.class);
            data = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        if(!Objects.isNull(data)){
            List<CashFlowItem> items =  data.getData().getItems();

            items.forEach(item ->{
                CashFlowEntity entity = new CashFlowEntity();
                entity.setTicker(item.getTicker());
                entity.setNetCashFlowFromOperatingActivities(item.getCf1() / 1000000);
                entity.setNetCashFlowFromFinancingActivities(item.getCf30() / 1000000);
                entity.setNetCashFlowFromInvestingActivities(item.getCf22() / 1000000);
                entity.setNetCashFlowForThePeriod(item.getCf37() / 1000000);
                entity.setBeginningCashAndCashEquivalents(item.getCf38() / 1000000);
                entity.setEndingCashAndCashEquivalents(item.getCf40() / 1000000);
                String quarter = item.getPeriodDateName();
                entity.setQuarter(quarter);
                entity.setHashQuarter(DigestUtils.sha256Hex(quarter + item.getTicker()));
                cashFlowRepository.save(entity);
                log.info("Luu du lieu balance sheet cho quy {} thanh cong ", quarter);
            });
        } else
            log.info("Khong tim thay du lieu balance sheet cho cong ty {}", ticker);

    }
}
