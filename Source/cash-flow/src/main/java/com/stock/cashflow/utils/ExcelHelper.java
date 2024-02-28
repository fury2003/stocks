package com.stock.cashflow.utils;

import com.stock.cashflow.constants.FSConstant;
import com.stock.cashflow.constants.IndustryConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.exception.BadRequestException;
import com.stock.cashflow.persistence.entity.*;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class ExcelHelper {

    private static final Logger log = LoggerFactory.getLogger(ExcelHelper.class);

    @Value("${data.trading.file.path}")
    private String dataTradingFile;

    @Value("${statistics.file.path}")
    private String statisticFile;

    @Value("${fs.file.path}")
    private String fsFilePath;

    @Value("${statistics.insert.new.row.index}")
    private int statisticsBeginRowIndex;

    @Value("${fs.insert.new.row.index}")
    private int fsBeginRowIndex;

    @Autowired
    Environment env;

    public int findRowIndexByCellValue(String filePath, String sheetName, int columnIndex, int rowIndexStart, int rowIndexEnd, String targetValue) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            for (int rowIndex = rowIndexStart - 1; rowIndex <= rowIndexEnd; rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row != null) {
                    Cell cell = row.getCell(columnIndex - 1);

                    if (cell != null) {
                        try {
                            if (targetValue.equals(cell.getStringCellValue()))
                                return rowIndex;
                        } catch (IllegalStateException ex) {
                            log.error("Format date khong dung o cot date trong file Excel. {}", cell.getStringCellValue());
                            throw new BadRequestException("Format date khong dung o cot date trong file Excel.");
                        }
                    }
                }
            }

            // If the value is not found, return -1 or handle accordingly
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return -1;
        }
    }


    public static List<String> getsheetNames(String filePath) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            List<String> sheetNames = getSheetNames(workbook);
            List<String> output = new ArrayList<>();
            for (String sheetName : sheetNames) {
                if(!sheetName.contains(StockConstant.STATISTIC_INGORE_SHEET)){
                    output.add(sheetName.replace("-Q4", ""));
                }
            }
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", filePath);
            return null;
        }
    }

    public static List<String> getSheetNames(Workbook workbook) {
        List<String> sheetNames = new ArrayList<>();
        int numberOfSheets = workbook.getNumberOfSheets();

        for (int i = 0; i < numberOfSheets; i++) {
            sheetNames.add(workbook.getSheetName(i));
        }

        return sheetNames;
    }

    public void writeIntradayTradingStatisticsToFile(String sheetName, TradingStatistics data) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(dataTradingFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, statisticsBeginRowIndex);
            Row row = sheet.getRow(statisticsBeginRowIndex);
            log.info("Mo file va insert dong moi thanh cong");

            double foreignBuyVolume = data.getForeignBuyVolume();
            double foreignSellVolume = data.getForeignSellVolume();
            double foreignBuyValue = data.getForeignBuyValue();
            double foreignSellValue = data.getForeignSellValue();

            if(!Objects.isNull(data.getProprietaryBuyVolume())){
                double proprietaryBuyVolume = data.getProprietaryBuyVolume();
                double proprietarySellVolume = data.getProprietarySellVolume();
                double proprietaryBuyValue = data.getProprietaryBuyValue();
                double proprietarySellValue = data.getProprietarySellValue();
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX), proprietaryBuyVolume, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX), proprietarySellVolume, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX), proprietaryBuyVolume - proprietarySellVolume, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX), proprietaryBuyValue - proprietarySellValue, false);
            }

            if(!Objects.isNull(data.getBuyOrder())){
                double buyOrder = data.getBuyOrder();
                double sellOrder = data.getSellOrder();
                double bigBuyOrderVol = data.getBigBuyOrder();
                double bigSellOrderVol = data.getBigSellOrder();
                double buyOrderVol = data.getBuyOrderVolume();
                double sellOrderVol = data.getSellOrderVolume();
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX), buyOrder, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX), sellOrder, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BIG_BUY_ORDER_COLUMN_INDEX), bigBuyOrderVol, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BIG_SELL_ORDER_COLUMN_INDEX), bigSellOrderVol, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX), buyOrderVol, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX), sellOrderVol, false);

                if(buyOrder > sellOrder){
                    String conditionFormula = "L3>M3";
                    applyConditionalFormattingOrderBookStrongBuy(sheet, conditionFormula);
                } else if(buyOrder < sellOrder){
                    String conditionFormula = "L3<M3";
                    applyConditionalFormattingOrderBookStrongSell(sheet, conditionFormula);
                }
            }

            double totalVolume = data.getTotalVolume();
            String percenChange = data.getPercentageChange().replace("%", "");
            String priceRange = data.getPriceRange().replace("%", "");


            updateCellDate(workbook, row, getExcelColumnIndex(StockConstant.TRADING_DATE_COLUMN_INDEX), data.getTradingDate());
            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_BUY_VOL_COLUMN_INDEX), foreignBuyVolume, false);
            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_SELL_VOL_COLUMN_INDEX), foreignSellVolume, false);
            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_TOTAL_NET_VOL_COLUMN_INDEX), foreignBuyVolume - foreignSellVolume, false);
            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_TOTAL_NET_VAL_COLUMN_INDEX), foreignBuyValue - foreignSellValue, false);

            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.TOTAL_VOL_COLUMN_INDEX), totalVolume, false);
            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PERCENTAGE_CHANGE_COLUMN_INDEX), Double.parseDouble(percenChange)/100, true);
            updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PRICE_RANGE_COLUMN_INDEX), Double.parseDouble(priceRange)/100, true);

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(dataTradingFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", dataTradingFile);
        }
    }

    public void writeVolatileForeignTradingToFile(String sheetName, String duration, List<ForeignTradingStatisticEntity> updatedList, boolean isTNV) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);

            Instant instant = new Date().toInstant();
            LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            updateCellDate(workbook, row, 1, today.toString());
            updateCellString(row, 2, duration);

            for (int i = 0; i < updatedList.size(); i++) {
                if(isTNV){
                    Double tnv = sheetName.equals(StockConstant.VOLATILE_FOREIGN_BUY) ? updatedList.get(i).getOneMonthHighestBuyValue() :  updatedList.get(i).getOneMonthHighestSellValue();
                    updateCellDouble(workbook, row, i+3, tnv, false);
                }else {
                    updateCellString(row, i+3, updatedList.get(i).getSymbol());
                }
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }
    }

    public void writeVolatileProprietaryTradingToFile(String sheetName, String duration, List<ProprietaryTradingStatisticEntity> updatedList, boolean isTNV) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);

            Instant instant = new Date().toInstant();
            LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            updateCellDate(workbook, row, 1, today.toString());
            updateCellString(row, 2, duration);

            for (int i = 0; i < updatedList.size(); i++) {
                if(isTNV){
                    Double tnv = sheetName.equals(StockConstant.VOLATILE_PROPRIETARY_BUY) ? updatedList.get(i).getOneMonthHighestBuyValue() :  updatedList.get(i).getOneMonthHighestSellValue();
                    updateCellDouble(workbook, row, i+3, tnv, false);
                }else {
                    updateCellString(row, i+3, updatedList.get(i).getSymbol());
                }
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }
    }

    public void writeQuarterFinancialStatementToFile(String sheetName, BalanceSheetEntity balanceSheet, IncomeSheetEntity incomeSheet, CashFlowEntity cashFlow) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(fsFilePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, fsBeginRowIndex);
            Row row = sheet.getRow(fsBeginRowIndex);
            log.info("Mo file va insert dong moi thanh cong");

            updateCellString(row, getExcelColumnIndex(FSConstant.FS_QUARTER_COL_IDX), balanceSheet.getQuarter());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_REVENUE_COL_IDX), incomeSheet.getNetRevenue());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_COGS_COL_IDX), incomeSheet.getCogs());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_GROSS_PROFIT_COL_IDX), incomeSheet.getGrossProfit());

            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_INTEREST_COST_COL_IDX), incomeSheet.getInterestCost());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_INCOME_ATTRIBUTEABLE_TO_PARENT_COL_IDX), incomeSheet.getNetIncomeAttributableToParent());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_EQUITY_COL_IDX), balanceSheet.getEquity());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_TOTAL_ASSETS_COL_IDX), balanceSheet.getTotalAssets());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_LIABILITIES_COL_IDX), balanceSheet.getLiabilities());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_SHORT_TERM_ASSETS_COL_IDX), balanceSheet.getShortTermAssets());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_CURRENT_LIABILITIES_COL_IDX), balanceSheet.getCurrentLiabilities());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_INVENTORY_COL_IDX), balanceSheet.getInventory());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_ACCOUNTS_RECEIVABLE_COL_IDX), balanceSheet.getAccountsReceivable());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_CACE_COL_IDX), balanceSheet.getCashAndCashEquivalents());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_SHORT_TERM_INVESTMENTS_COL_IDX), balanceSheet.getShortTermInvestments());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_OPERATING_COL_IDX), cashFlow.getNetCashFlowFromOperatingActivities());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_INVESTING_COL_IDX), cashFlow.getNetCashFlowFromInvestingActivities());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_FINANCING_COL_IDX), cashFlow.getNetCashFlowFromFinancingActivities());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_FOR_THE_PERIOD_COL_IDX), cashFlow.getNetCashFlowForThePeriod());

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(fsFilePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        }catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", fsFilePath);
        }

    }

    public void writeMultipleQuarterFinancialStatementToFile(String sheetName, List<BalanceSheetEntity> balanceSheets, List<IncomeSheetEntity> incomeSheets, List<CashFlowEntity> cashFlows) {

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(fsFilePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if(balanceSheets.size() == incomeSheets.size()){
                for (int i = 0; i < balanceSheets.size(); i++) {
                    insertNewRow(sheet, fsBeginRowIndex);
                    Row row = sheet.getRow(fsBeginRowIndex);
                    updateCellString(row, getExcelColumnIndex(FSConstant.FS_QUARTER_COL_IDX), balanceSheets.get(i).getQuarter());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_REVENUE_COL_IDX), incomeSheets.get(i).getNetRevenue());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_COGS_COL_IDX), incomeSheets.get(i).getCogs());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_GROSS_PROFIT_COL_IDX), incomeSheets.get(i).getGrossProfit());

                    // bank, stock
                    if(checkFinancialStock(IndustryConstant.BANKS, sheetName) || checkFinancialStock(IndustryConstant.STOCKS, sheetName)){
                        updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_OPERATING_EXPENSES_COL_IDX), incomeSheets.get(i).getOperatingExpenses());
                    } else {
                        updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_SELLING_EXPENSES_COL_IDX), incomeSheets.get(i).getSellingExpenses());
                    }

                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_INTEREST_COST_COL_IDX), incomeSheets.get(i).getInterestCost());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_INCOME_ATTRIBUTEABLE_TO_PARENT_COL_IDX), incomeSheets.get(i).getNetIncomeAttributableToParent());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_EQUITY_COL_IDX), balanceSheets.get(i).getEquity());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_TOTAL_ASSETS_COL_IDX), balanceSheets.get(i).getTotalAssets());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_LIABILITIES_COL_IDX), balanceSheets.get(i).getLiabilities());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_SHORT_TERM_ASSETS_COL_IDX), balanceSheets.get(i).getShortTermAssets());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_CURRENT_LIABILITIES_COL_IDX), balanceSheets.get(i).getCurrentLiabilities());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_INVENTORY_COL_IDX), balanceSheets.get(i).getInventory());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_ACCOUNTS_RECEIVABLE_COL_IDX), balanceSheets.get(i).getAccountsReceivable());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_CACE_COL_IDX), balanceSheets.get(i).getCashAndCashEquivalents());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_SHORT_TERM_INVESTMENTS_COL_IDX), balanceSheets.get(i).getShortTermInvestments());

                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_OPERATING_COL_IDX), i >= cashFlows.size() ? 0 : cashFlows.get(i).getNetCashFlowFromOperatingActivities());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_INVESTING_COL_IDX), i >= cashFlows.size() ? 0 : cashFlows.get(i).getNetCashFlowFromInvestingActivities());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_FINANCING_COL_IDX), i >= cashFlows.size() ? 0 : cashFlows.get(i).getNetCashFlowFromFinancingActivities());
                    updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_CF_FOR_THE_PERIOD_COL_IDX), i >= cashFlows.size() ? 0 : cashFlows.get(i).getNetCashFlowForThePeriod());
                    log.info("Cap nhat du lieu quy {} cho cong ty {} thanh cong.", balanceSheets.get(i).getQuarter(), sheetName);
                }
                // Save the workbook to a file
                try (FileOutputStream fileOut = new FileOutputStream(fsFilePath)) {
                    workbook.write(fileOut);
                    log.info("Cap nhat du lieu vao file Excel thanh cong.");
                }
            }else{
                log.warn("Du lieu trong balance sheet, income sheet, cash flow ko khop");
                throw  new RuntimeException("Du lieu trong balance sheet, income sheet, cash flow ko khop");
            }

        }catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", fsFilePath);
        }

    }

    private static boolean checkFinancialStock(String[] array, String targetValue) {
        for (String element : array) {
            if (element.equals(targetValue)) {
                return true;
            }
        }
        return false;
    }

    public void updateISSpecificColumnFromTo(String sheetName, String updatedQuarter, int columnIdx, long value) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(fsFilePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            int tradingDateIdx = getExcelColumnIndex(StockConstant.TRADING_DATE_COLUMN_INDEX);
            boolean isChanged = false;

            for (int rowIndex = 3 - 1; rowIndex <= 100; rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row != null) {
                    Cell cell = row.getCell(tradingDateIdx);
                    if (cell != null) {
                        try {
                            if (updatedQuarter.equals(cell.getStringCellValue())){
                                updateCellLong(workbook, row, columnIdx, value);
                                isChanged = true;
                                break;
                            }
                        } catch (IllegalStateException ex) {
                            log.error("Format date khong dung o cot date trong file Excel. {}", cell.getStringCellValue());
                            throw new BadRequestException("Format date khong dung o cot date trong file Excel.");
                        }
                    }
                }
            }

            if(isChanged){
                try (FileOutputStream fileOut = new FileOutputStream(fsFilePath)) {
                    workbook.write(fileOut);
                    log.info("Cap nhat du lieu vao file Excel thanh cong.");
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", fsFilePath);
        }
    }

    private int getExcelColumnIndex(String columnName){
        return Integer.parseInt(env.getProperty(columnName));
    }

    public static void insertNewRow(Sheet sheet, int rowIndex) {
        // Shift existing rows down to make space for the new row
        int lastRow = sheet.getLastRowNum();
        int physicalRow = sheet.getPhysicalNumberOfRows();
        log.info("sheet name: {} , lastRow: {}, physicalRow: {}", sheet.getSheetName(), lastRow, physicalRow);
        sheet.shiftRows(rowIndex, lastRow, 1, true, true);
        sheet.createRow(rowIndex);

    }

    public static void updateCellPercentage(Workbook workbook, Row row, int columnIndex, Double value, int color) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        CellStyle percentageStyle = workbook.createCellStyle();
        percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        cell.setCellValue(value);

        if(color == 1){
            percentageStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        } else if(color == 2){
            percentageStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        }

        percentageStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(percentageStyle);

    }

    public static void updateCellDouble(Workbook workbook, Row row, int columnIndex, Double value, boolean isPercentage) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        if (isPercentage) {
            CellStyle percentageStyle = workbook.createCellStyle();
            percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
            cell.setCellValue(value);
            cell.setCellStyle(percentageStyle);
            return;
        }

        CellStyle numberStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        numberStyle.setDataFormat(format.getFormat("#,##0"));
        cell.setCellValue(value);
        cell.setCellStyle(numberStyle);
    }

    public static void applyConditionalFormattingOrderBookStrongBuy(Sheet sheet, String conditionFormula) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(ComparisonOperator.GT, conditionFormula, null);

        PatternFormatting fillFmt = rule.createPatternFormatting();
        fillFmt.setFillBackgroundColor(IndexedColors.GREEN.index);
        fillFmt.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] order = { CellRangeAddress.valueOf("L3"), CellRangeAddress.valueOf("M3") };
        sheetCF.addConditionalFormatting(order, rule);
    }

    public static void applyConditionalFormattingOrderBookStrongSell(Sheet sheet, String conditionFormula) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, conditionFormula, null);

        PatternFormatting fillFmt = rule.createPatternFormatting();
        fillFmt.setFillBackgroundColor(IndexedColors.RED.index);
        fillFmt.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] order = { CellRangeAddress.valueOf("L3"), CellRangeAddress.valueOf("M3") };
        sheetCF.addConditionalFormatting(order, rule);
    }

    public static void updateCellInt(Workbook workbook, Row row, int columnIndex, int value) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        CellStyle numberStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        numberStyle.setDataFormat(format.getFormat("#,##0"));
        cell.setCellValue(value);
        cell.setCellStyle(numberStyle);
    }

    public static void updateCellLong(Workbook workbook, Row row, int columnIndex, long value) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        CellStyle numberStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        numberStyle.setDataFormat(format.getFormat("#,##0"));
        cell.setCellValue(value);
        cell.setCellStyle(numberStyle);
    }

    public static void updateCellString(Row row, int columnIndex, String value) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        cell.setCellValue(value);
    }

    public static void updateCellDate(Workbook workbook, Row row, int columnIndex, String value) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setAlignment(HorizontalAlignment.RIGHT);
        CreationHelper creationHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(value);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = outputFormat.format(date);
            cell.setCellValue(formattedDate);
        }catch (Exception ex){
            log.error("Can not parse date");
            cell.setCellValue(value);

        }
        cell.setCellStyle(dateStyle);

    }
}
