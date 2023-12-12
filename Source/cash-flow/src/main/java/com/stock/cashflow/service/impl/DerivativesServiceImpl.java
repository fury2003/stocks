package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockContants;
import com.stock.cashflow.dto.DerivativesDTO;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.service.DerivativesService;
import com.stock.cashflow.utils.DateHelper;
import com.stock.cashflow.utils.ExcelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class DerivativesServiceImpl implements DerivativesService {

    private static final Logger log = LoggerFactory.getLogger(DerivativesServiceImpl.class);

    @Value("${statistics.file.path}")
    private String filePath;

    @Value("${foreign.api.host.baseurl}")
    private String foreignAPIHostAPIHost;

    @Value("${statistics.derivatives.date.column.index}")
    private int columnIndex;

    @Value("${statistics.derivatives.date.row.index.start}")
    private int rowIndexStart;

    @Value("${statistics.derivatives.date.row.index.end}")
    private int rowIndexEnd;

    @Autowired
    Environment env;

    private final RestTemplate restTemplate;

    private final ExcelHelper excelHelper;

    public DerivativesServiceImpl(RestTemplate restTemplate, ExcelHelper excelHelper){
        this.restTemplate = restTemplate;
        this.excelHelper = excelHelper;
    }

    @Override
    public void process(String symbol, String startDate, String endDate, String token) {
        log.info("Bat dau cap nhat du lieu phai sinh cua khoi ngoai: {}", symbol);

        String url = foreignAPIHostAPIHost + "symbols/" + symbol + "/historical-quotes?startDate=" + startDate + "&endDate=" + endDate + "&offset=0&limit=100";
        HttpHeaders headers = new HttpHeaders();
        headers.set(StockContants.AUTHORIZATION, token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Symbol[] data = null;
        try{
            ResponseEntity<Symbol[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Symbol[].class);
            data = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
            throw ex;
        }

        excelHelper.updateDerivatives(filePath, symbol, columnIndex, rowIndexStart, rowIndexEnd, data);

        log.info("Cap nhat du lieu phai sinh cua khoi ngoai tu ngay: {} den ngay {} thanh cong", startDate, endDate);
        log.info("Ket thuc cap nhat du lieu phai sinh cua khoi ngoai:" + symbol);
    }

}
