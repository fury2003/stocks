package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.exception.BadRequestException;
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
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class StockValuationServiceImpl implements StockValuationService {

    private static final Logger log = LoggerFactory.getLogger(StockValuationServiceImpl.class);

    private final StockPriceRepository stockPriceRepository;
    private final IncomeSheetRepository incomeSheetRepository;
    private final BalanceSheetRepository balanceSheetRepository;
    private final CashFlowRepository cashFlowRepository;
    private final ExcelHelper excelHelper;

    @Autowired
    Environment env;

    @Value("${fs.file.path}")
    private String fsFilePath;

    public StockValuationServiceImpl(StockPriceRepository stockPriceRepository, IncomeSheetRepository incomeSheetRepository, BalanceSheetRepository balanceSheetRepository, CashFlowRepository cashFlowRepository, ExcelHelper excelHelper){
        this.stockPriceRepository = stockPriceRepository;
        this.incomeSheetRepository = incomeSheetRepository;
        this.balanceSheetRepository = balanceSheetRepository;
        this.cashFlowRepository = cashFlowRepository;
        this.excelHelper = excelHelper;
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
        List<BalanceSheetEntity> balanceSheetEntities = balanceSheetRepository.findBalanceSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
        List<CashFlowEntity> cashFlowEntities = cashFlowRepository.findCashFlowEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);

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

            for (int i = 40; i < 200; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    log.info("Ko tim thay ma chung khoan");
                    break;
                }

                Cell symbolCell = row.getCell(0);
                if (symbolCell != null) {
                    try {
                        String symbol = symbolCell.getStringCellValue();
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
                    } catch (Exception ex) {
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
}
