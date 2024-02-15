package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.FSConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.FinancialInfoItem;
import com.stock.cashflow.dto.FinancialInfoSheetResponse;
import com.stock.cashflow.persistence.entity.BalanceSheetEntity;
import com.stock.cashflow.persistence.entity.CashFlowEntity;
import com.stock.cashflow.persistence.entity.IncomeSheetEntity;
import com.stock.cashflow.persistence.repository.BalanceSheetRepository;
import com.stock.cashflow.persistence.repository.CashFlowRepository;
import com.stock.cashflow.persistence.repository.IncomeSheetRepository;
import com.stock.cashflow.persistence.repository.StockPriceRepository;
import com.stock.cashflow.service.StockValuationService;
import com.stock.cashflow.utils.ExcelHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StockValuationServiceImpl implements StockValuationService {

    private static final Logger log = LoggerFactory.getLogger(StockValuationServiceImpl.class);

    private final StockPriceRepository stockPriceRepository;
    private final IncomeSheetRepository incomeSheetRepository;
    private final BalanceSheetRepository balanceSheetRepository;
    private final CashFlowRepository cashFlowRepository;
    private final ExcelHelper excelHelper;
    private final RestTemplate restTemplate;

    @Autowired
    Environment env;

    @Value("${fs.file.path}")
    private String fsFilePath;

    @Value("${fa.api.host.baseurl}")
    private String bsAPI;

    public StockValuationServiceImpl(StockPriceRepository stockPriceRepository, IncomeSheetRepository incomeSheetRepository, BalanceSheetRepository balanceSheetRepository, CashFlowRepository cashFlowRepository, ExcelHelper excelHelper, RestTemplate restTemplate){
        this.stockPriceRepository = stockPriceRepository;
        this.incomeSheetRepository = incomeSheetRepository;
        this.balanceSheetRepository = balanceSheetRepository;
        this.cashFlowRepository = cashFlowRepository;
        this.excelHelper = excelHelper;
        this.restTemplate = restTemplate;
    }

    @Override
    public void writeDataForSpecificQuarter(String ticker, String quarter) {
        String hashQuarter = DigestUtils.sha256Hex(quarter + ticker);
        IncomeSheetEntity incomeSheetEntity = incomeSheetRepository.findIncomeSheetEntitiesByHashQuarter(hashQuarter);
        BalanceSheetEntity balanceSheetEntity = balanceSheetRepository.findBalanceSheetEntitiesByHashQuarter(hashQuarter);
        CashFlowEntity cashFlowEntity = cashFlowRepository.findCashFlowEntitiesByHashQuarter(hashQuarter);
        if(!Objects.isNull(incomeSheetEntity) && !Objects.isNull(balanceSheetEntity) && !Objects.isNull(cashFlowEntity))
            excelHelper.writeQuarterFinancialStatementToFile(ticker, balanceSheetEntity, incomeSheetEntity, cashFlowEntity);
        else{
            throw new RuntimeException("Ko tim thay bao cao tai chinh cua cong ty " + ticker);
        }

        log.info("Ghi du lieu bao cao tai chinh cho {} cua cong ty {} thanh cong", quarter, ticker);
    }

    @Override
    public void writeDataFromQuarterTo(String ticker, String fromQuarter, String toQuarter) {
        List<IncomeSheetEntity> incomeSheetEntities = incomeSheetRepository.findIncomeSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
//        if (!incomeSheetEntities.isEmpty()) {
//            IncomeSheetEntity firstItem = incomeSheetEntities.remove(0);
//            incomeSheetEntities.add(firstItem);
//        }

        List<BalanceSheetEntity> balanceSheetEntities = balanceSheetRepository.findBalanceSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
//        if (!balanceSheetEntities.isEmpty()) {
//            BalanceSheetEntity firstItem = balanceSheetEntities.remove(0);
//            balanceSheetEntities.add(firstItem);
//        }

        List<CashFlowEntity> cashFlowEntities = cashFlowRepository.findCashFlowEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
//        if (!cashFlowEntities.isEmpty()) {
//            CashFlowEntity firstItem = cashFlowEntities.remove(0);
//            cashFlowEntities.add(firstItem);
//        }

        if(!incomeSheetEntities.isEmpty() && !balanceSheetEntities.isEmpty() && !cashFlowEntities.isEmpty())
            excelHelper.writeMultipleQuarterFinancialStatementToFile(ticker, balanceSheetEntities, incomeSheetEntities, cashFlowEntities);
        else
            log.warn("Ko tim thay bao cao tai chinh cua cong ty {}", ticker);
        log.info("Ghi du lieu bao cao tai chinh tu {} den {} cua cong ty {} thanh cong", fromQuarter, toQuarter, ticker);

    }

    @Override
    public void writeDataForSpecificYear(String ticker, String quarter) {

    }

    @Override
    public void writeDataFromToForSpecificColumn(String ticker, String fromQuarter, String toQuarter, int column) {
        switch (column){
            case 8 -> {
                List<IncomeSheetEntity> incomeSheetEntities = incomeSheetRepository.findIncomeSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
                incomeSheetEntities.forEach(item -> {
                    excelHelper.updateISSpecificColumnFromTo(ticker, item.getQuarter(), column, item.getNetIncomeAttributableToParent());
                });
            }

            default -> log.info("Updated column not found");
        }

    }

    @Override
    public void updateLatestPrice(String tradingDate) {

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(fsFilePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(StockConstant.PRICE_VALUE);

            for (int i = 40; i < 250; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    log.info("Ko tim thay ma chung khoan");
                    break;
                }

                Cell symbolCell = row.getCell(0);
                if (symbolCell != null) {
                    String symbol = symbolCell.getStringCellValue();
                    try {
                        log.info("Symbol: {}", symbol);
                        String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);
                        long closePrice = stockPriceRepository.getClosedPriceByHashDate(hashDate);
                        log.info("Gia dong cua cua {} la {}", symbol, closePrice);
                        Cell priceCell = row.getCell(1);
                        if (priceCell != null) {
                            CellStyle numberStyle = workbook.createCellStyle();
                            DataFormat format = workbook.createDataFormat();
                            numberStyle.setDataFormat(format.getFormat("#,##0"));
                            priceCell.setCellValue(closePrice);
                            priceCell.setCellStyle(numberStyle);
                        }
                    }catch (NullPointerException ex){
                        log.warn("Can't found close price of symbol {}: ", symbol);
                    }catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException("Loi trong qua trinh cap nhat gia moi nhat");
                    }
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream(fsFilePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }


        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", fsFilePath);
        }
    }

    @Override
    public void getGeneralInfoForAll(String year) {
        String[] symbols = SymbolConstant.SYMBOLS;
        try (FileInputStream fileInputStream = new FileInputStream(fsFilePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {

        for (String symbol : symbols) {
            String url = bsAPI + "ratio/" + symbol + "?period=Y&size=3";

            AtomicLong dt = new AtomicLong();
            AtomicLong ln = new AtomicLong();
            AtomicLong vg = new AtomicLong();
            AtomicLong vcsh = new AtomicLong();
            AtomicLong slcp = new AtomicLong();

            try{
                ResponseEntity<FinancialInfoSheetResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, FinancialInfoSheetResponse.class);
                FinancialInfoSheetResponse data = response.getBody();
                if(!Objects.isNull(data)) {
                    List<FinancialInfoItem> items = data.getData().getItems();
                    log.info("Truy xuat thong tin nam {} cho ma {}", year, symbol);
                    items.forEach(item ->{
                        if(item.getPeriodDate().equals(year)){
                            // bank use is9. other use is1
                            Long income = item.getIs9() == null ? item.getIs1() : item.getIs9();
                            dt.set(income / 1000000);
                            ln.set(item.getIs14() / 1000000);
                            vcsh.set(item.getBs10() / 1000000);
                            vg.set(item.getBs11() / 1000000);
                            slcp.set(item.getOp49());
                        }
                    });
                }
            } catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu");
                throw new RuntimeException("Loi trong qua trinh truy xuat du lieu");
            }

            Sheet sheet = workbook.getSheet(year);
            excelHelper.insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);
            excelHelper.updateCellString(row, 1, symbol);
            excelHelper.updateCellLong(workbook, row, 2, vg.longValue());
            excelHelper.updateCellLong(workbook, row, 3, vcsh.longValue());
            excelHelper.updateCellLong(workbook, row, 4, slcp.longValue());
            excelHelper.updateCellLong(workbook, row, 5, dt.longValue());
            excelHelper.updateCellLong(workbook, row, 6, ln.longValue());
        }

        try (FileOutputStream fileOut = new FileOutputStream(fsFilePath)) {
            workbook.write(fileOut);
            log.info("Cap nhat du lieu vao file Excel thanh cong.");
        }

        }catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", fsFilePath);
            throw new RuntimeException(("Loi trong qua trinh xu ly file"));
        }

    }

    @Override
    public void getGeneralInfo(String ticker, String year) {
        String url = bsAPI + "ratio/" + ticker + "?period=Y&size=3";

        AtomicLong dt = new AtomicLong();
        AtomicLong ln = new AtomicLong();
        AtomicLong vg = new AtomicLong();
        AtomicLong vcsh = new AtomicLong();
        AtomicLong slcp = new AtomicLong();

        try{
            ResponseEntity<FinancialInfoSheetResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, FinancialInfoSheetResponse.class);
            FinancialInfoSheetResponse data = response.getBody();
            if(!Objects.isNull(data)) {
                List<FinancialInfoItem> items = data.getData().getItems();
                items.forEach(item ->{
                    log.info("Truy xuat thong tin nam {} cho ma {}", year, ticker);
                    if(item.getPeriodDate().equals(year)){
                        Long income = item.getIs9() == null ? item.getIs1() : item.getIs9();
                        dt.set(income / 1000000);
                        ln.set(item.getIs14() / 1000000);
                        vcsh.set(item.getBs10() / 1000000);
                        vg.set(item.getBs11() / 1000000);
                        slcp.set(item.getOp49());
                    }
                });
            }
        } catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw new RuntimeException("Loi trong qua trinh truy xuat du lieu");
        }

        try (FileInputStream fileInputStream = new FileInputStream(fsFilePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(year);
            excelHelper.insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);
            excelHelper.updateCellString(row, 1, ticker);
            excelHelper.updateCellLong(workbook, row, 2, vg.longValue());
            excelHelper.updateCellLong(workbook, row, 3, vcsh.longValue());
            excelHelper.updateCellLong(workbook, row, 4, slcp.longValue());
            excelHelper.updateCellLong(workbook, row, 5, dt.longValue());
            excelHelper.updateCellLong(workbook, row, 6, ln.longValue());

            try (FileOutputStream fileOut = new FileOutputStream(fsFilePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        }catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", fsFilePath);
            throw new RuntimeException(("Loi trong qua trinh xu ly file"));
        }
    }
}
