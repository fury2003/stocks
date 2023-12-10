package com.stock.cashflow.service.impl;


import com.stock.cashflow.constants.StockContants;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.exception.BadRequestException;
import com.stock.cashflow.service.IndexService;
import com.stock.cashflow.utils.ExcelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
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

    @Value("${file.path}")
    private String filePath;

    @Value("${index.sheet.name}")
    private String indexSheetName;

    @Value("${foreign.api.host.baseurl}")
    private String foreignAPIHost;

    @Value("${proprietary.api.host.baseurl}")
    private String proprietaryAPIHost;

    @Value("${index.date.column.index}")
    private int columnIndex;

    @Value("${index.date.row.index.start}")
    private int rowIndexStart;

    @Value("${index.date.row.index.end}")
    private int rowIndexEnd;

    @Autowired
    Environment env;

    private final RestTemplate restTemplate;

    private final ExcelHelper excelHelper;

    public IndexServiceImpl(RestTemplate restTemplate, ExcelHelper excelHelper){
        this.restTemplate = restTemplate;
        this.excelHelper = excelHelper;
    }

    @Override
    public void process(String index, String date, String token) {
        log.info("Bat dau lay du lieu giao dich ma chung khoan: {}", index);

        String url = foreignAPIHost + "symbols/" + index + "/historical-quotes?startDate=" + date + "&endDate=" + date + "&offset=0&limit=1";
        HttpHeaders headers = new HttpHeaders();
        headers.set(StockContants.AUTHORIZATION, token);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        Index data = null;
        try{
            ResponseEntity<Index[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Index[].class);
            if(Arrays.stream(Objects.requireNonNull(response.getBody())).findAny().isEmpty()){
                log.info("Khong tim thay du lieu cho ma chung khoan {} ngay {}", index, date);
                throw new BadRequestException("Khong tim thay du lieu cua ma " + index + " cho ngay " + date);
            }
            data = Arrays.stream(response.getBody()).findFirst().get();

        }catch (BadRequestException ex){
            throw ex;
        }
        catch (Exception ex){
            log.error("error", ex);
            throw new RuntimeException("Loi trong qua trinh lay du lieu tu Fireant");
        }

        log.info("Ket thuc lay du lieu giao dich cho {}", index);
        log.info("Bat dau cap nhat du lieu giao dich cho {}", index);

        int rowIndex = excelHelper.findRowIndexByCellValue(filePath, indexSheetName, columnIndex, rowIndexStart, rowIndexEnd, date);

        if (rowIndex != -1) {
            log.info("Tim thay ngay '{}' tai dong {}", date, rowIndex);
        } else {
            log.error("Khong tim thay cho ngay {} tai cot {}.", date, rowIndex);
            throw new RuntimeException("Khong tim thay cho ngay " + date + " tai cot " + rowIndex);
        }

        excelHelper.excelIndexWriter(filePath, indexSheetName, rowIndex, data);


        log.info("Cap nhat du lieu vao dong : {}, cot {}", rowIndex, columnIndex);

        log.info("Ket thuc cap nhat du lieu giao dich ma chung khoan: {}", index);
    }

    @Override
    public void processProprietaryTradingValue(String index, String quarter) {
        log.info("Bat dau lay du lieu tu doanh tren san: {}", index);
        long epochTime = Instant.now().toEpochMilli();

        String url = proprietaryAPIHost + "ComGroupCode=" + index + "&time=" + epochTime;

        ProprietaryDataResponse proprietaryDataResponse = null;
        try{
            ResponseEntity<ProprietaryDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, ProprietaryDataResponse.class);
            proprietaryDataResponse = response.getBody();
        }catch (Exception ex){
            throw ex;
        }

        String statisticFilePath = env.getProperty(StockContants.STATISTIC_FILE_PATH);
        int columnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_DATE_COLUMN_INDEX));
        int rowIndexStart = Integer.parseInt(env.getProperty(StockContants.STATISTIC_DATE_ROW_START_INDEX));
        int rowIndexEnd = Integer.parseInt(env.getProperty(StockContants.STATISTIC_DATE_ROW_END_INDEX));

        List<ProprietaryItems> items = proprietaryDataResponse.getItems();
        Optional<ProprietaryItems> proprietaryDataOptional = items.stream().findFirst();
        if(proprietaryDataOptional.isPresent()){
            ProprietaryToday proprietaryToday = proprietaryDataOptional.get().getToday();
            List<ProprietaryTrade> proprietaryTrades = proprietaryToday.getBuy();
            List<String> sheetNames = excelHelper.getsheetNames(statisticFilePath);

            List<ProprietaryTrade> filterData = proprietaryTrades.stream()
                    .filter(proprietaryTrade ->
                            sheetNames.stream().anyMatch(sheet -> sheet.equals(proprietaryTrade.getOrganCode())))
                    .collect(Collectors.toList());

            excelHelper.updateProprietaryCell(statisticFilePath, columnIndex, rowIndexStart, rowIndexEnd, filterData);
            log.info("Ket thuc cap nhat du lieu tu doanh cho sheet");
        }else
            log.info("Khong tim thay du lieu");


        log.info("Ket thuc lay du lieu tu doanh tren san: {}", index);
    }

}
