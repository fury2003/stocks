package com.stock.cashflow.service.impl;


import com.stock.cashflow.constants.StockContants;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.service.StockService;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

    @Value("${foreign.api.host.baseurl}")
    private String foreignAPIHostAPIHost;

    @Value("${intraday.latest.api.host.baseurl}")
    private String intradayAPIHost;

    @Value("${stockPrice.api.host.baseurl}")
    private String stockPriceAPIHost;

    @Value("${statistics.date.column.index}")
    private int columnIndex;

    @Value("${statistics.date.row.index.start}")
    private int rowIndexStart;

    @Value("${statistics.date.row.index.end}")
    private int rowIndexEnd;

    @Value("${statistics.file.path}")
    private String filePath;

    @Autowired
    Environment env;

    private final RestTemplate restTemplate;

    private final ExcelHelper excelHelper;

    public StockServiceImpl(RestTemplate restTemplate, ExcelHelper excelHelper){
        this.restTemplate = restTemplate;
        this.excelHelper = excelHelper;
    }

    @Override
    public void processStockPrice(String symbol, String startDate, String endDate) {
        log.info("Bat dau lay du lieu giao dich ma chung khoan: {}", symbol);
        String url = stockPriceAPIHost + symbol + "&fromDate=" + startDate + "&toDate=" + endDate;

        StockPriceDataResponse latestPriceDataResponse;
        try{
            ResponseEntity<StockPriceDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, StockPriceDataResponse.class);
            latestPriceDataResponse = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu SSI");
            throw ex;
        }

        List<StockPrice> items = latestPriceDataResponse.getData();
        LocalDateTime now = LocalDateTime.now();
        String quarter = DateHelper.determineQuarter(now.getMonthValue());
        String sheetName = String.join("-", symbol, quarter);
        excelHelper.updatePercentageCell(filePath , sheetName,  columnIndex, rowIndexStart, rowIndexEnd, items);

        log.info("Ket thuc lay du lieu giao dich ma chung khoan");
    }

    @Override
    public void processForeign(String symbol, String startDate, String endDate, String token) {
        log.info("Bat dau cap nhat du lieu khoi ngoai cho ma chung khoan: {}", symbol);

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


        Date firstDate = data[data.length-1].getDate();
        Instant instant = firstDate.toInstant();
        LocalDate firstDateLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        String quarter = DateHelper.determineQuarter(firstDateLocalDate.getMonthValue());
        String sheetName = String.join("-", symbol, quarter);

        excelHelper.updateForeignCell(filePath, sheetName, columnIndex, rowIndexStart, rowIndexEnd, data);

        log.info("Cap nhat du lieu khoi ngoai tu ngay: {} den ngay {} thanh cong", startDate, endDate);
        log.info("Ket thuc cap nhat du lieu khoi ngoai cho ma chung khoan: {}", symbol);
    }

    @Override
    public void processIntraday(String symbol, String date) {
        String url = intradayAPIHost + symbol;

        IntradayData data = null;
        try{
            ResponseEntity<IntradayData> response = restTemplate.exchange(url, HttpMethod.GET, null, IntradayData.class);
            data = response.getBody();
            log.info("Loi trong qua trinh truy xuat du lieu");
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        String statisticFilePath = env.getProperty(StockContants.STATISTIC_FILE_PATH);
        int columnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_DATE_COLUMN_INDEX));
        int startRowIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_DATE_ROW_START_INDEX));
        int endRowIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_DATE_ROW_END_INDEX));

        LocalDateTime today = LocalDateTime.now();
        String quarter = DateHelper.determineQuarter(today.getMonthValue());
        String sheetName = String.join("-", symbol, quarter);

        int rowIndex = excelHelper.findRowIndexByCellValue(statisticFilePath, sheetName, columnIndex, startRowIndex, endRowIndex, date);

        if (rowIndex != -1) {
            log.info("Tim thay ngay '{}' tai dong {}", date, rowIndex);
        } else {
            log.error("Khong tim thay cho ngay {} tai cot {}.", date, rowIndex);
            throw new RuntimeException("Khong tim thay cho ngay " + date + " tai cot " + rowIndex);
        }

        excelHelper.updateIntradayTrading(statisticFilePath, sheetName, rowIndex, data.getData());


    }

}
