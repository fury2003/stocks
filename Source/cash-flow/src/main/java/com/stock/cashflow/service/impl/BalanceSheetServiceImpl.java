package com.stock.cashflow.service.impl;

import com.stock.cashflow.dto.BalanceSheetItem;
import com.stock.cashflow.dto.BalanceSheetResponse;
import com.stock.cashflow.persistence.entity.BalanceSheetEntity;
import com.stock.cashflow.persistence.repository.BalanceSheetRepository;
import com.stock.cashflow.service.BalanceSheetService;
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
public class BalanceSheetServiceImpl implements BalanceSheetService {

    private static final Logger log = LoggerFactory.getLogger(BalanceSheetServiceImpl.class);

    private final BalanceSheetRepository balanceSheetRepository;
    private final RestTemplate restTemplate;


    public BalanceSheetServiceImpl(BalanceSheetRepository balanceSheetRepository, RestTemplate restTemplate){
        this.balanceSheetRepository = balanceSheetRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${fa.api.host.baseurl}")
    private String bsAPI;

    @Override
    public void crawlData(String ticker, String period, String size) {

        String url = bsAPI + "bs/" + ticker + "?period=" + period + "&size=" + size;

        BalanceSheetResponse data = null;
        try{
            ResponseEntity<BalanceSheetResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, BalanceSheetResponse.class);
            data = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        if(!Objects.isNull(data)){
            List<BalanceSheetItem> items =  data.getData().getItems();

            items.forEach(item ->{
                BalanceSheetEntity entity = new BalanceSheetEntity();
                entity.setTicker(item.getTicker());
                entity.setEquity(item.getBs10() / 1000000);
                entity.setTotalAssets(item.getBs1() / 1000000);
                entity.setShortTermAssets(item.getBs2() / 1000000);
                entity.setLongTermAssets(item.getBs3() / 1000000);
                entity.setLiabilities(item.getBs6() / 1000000);
                entity.setCurrentLiabilities(item.getBs8() / 1000000);
                entity.setLongTermLiabilities(item.getBs9() / 1000000);
                entity.setCashAndCashEquivalents(item.getBs13() / 1000000);
                entity.setShortTermInvestments(item.getBs16() / 1000000);
                entity.setAccountsReceivable(item.getBs20() / 1000000);
                entity.setInventory(item.getBs29() / 1000000);
                String quarter = item.getPeriodDateName();
                entity.setQuarter(quarter);
                entity.setHashQuarter(DigestUtils.sha256Hex(quarter + item.getTicker()));
                balanceSheetRepository.save(entity);
                log.info("Luu du lieu balance sheet cho quy {} thanh cong ", quarter);
            });
        } else
            log.info("Khong tim thay du lieu balance sheet cho cong ty {}", ticker);

    }
}
