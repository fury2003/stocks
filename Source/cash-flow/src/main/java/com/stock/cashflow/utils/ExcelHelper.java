package com.stock.cashflow.utils;

import com.stock.cashflow.constants.FSConstant;
import com.stock.cashflow.constants.IndustryConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.exception.BadRequestException;
import com.stock.cashflow.persistence.dto.SymbolTotalNetValueDTO;
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
import java.util.*;
import java.util.stream.IntStream;

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
                            if(targetValue.equals(cell.getStringCellValue()))
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

            if(!Objects.isNull(data.getProprietaryTotalNetValue())){
                double proprietaryBuyVolume = data.getProprietaryBuyVolume() == null ? 0 : data.getProprietaryBuyVolume();
                double proprietarySellVolume = data.getProprietarySellVolume() == null ? 0 : data.getProprietarySellVolume();;
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX), proprietaryBuyVolume, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX), proprietarySellVolume, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX), proprietaryBuyVolume - proprietarySellVolume, false);
                updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX), data.getProprietaryTotalNetValue(), false);
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

    public void writeTotalNetValueToFile(String sheetName, String duration, List<Double> updatedList, String tradingDate) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);

            if(duration.isEmpty())
                updateCellDate(workbook, row, 1, tradingDate);

            updateCellString(workbook, row, 2, duration);

            for (int i = 0; i < updatedList.size(); i++) {
                updateCellDouble(workbook, row, i+3, updatedList.get(i), false);
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

    public void writeVolatileForeignTradingToFile(String sheetName, String duration, List<ForeignTradingStatisticEntity> orderList, List<ForeignTradingStatisticEntity> updatedList) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);

            updateCellString(workbook, row, 2, duration);

            for (int i = 0; i < updatedList.size(); i++) {
                String symbol = updatedList.get(i).getSymbol();
                int updatedClx = i+3;
                OptionalInt optionalIndex = IntStream.range(0, orderList.size())
                        .filter(j -> orderList.get(j).getSymbol().equals(symbol))
                        .findFirst();
                if (optionalIndex.isPresent()) {
                    updatedClx = optionalIndex.getAsInt() + 3;
                }
                updateCellString(workbook, row, updatedClx, symbol);
                log.info("Cap nhat du lieu cua ma ck {} vao cot {}.",symbol, updatedClx);
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

    public void writeVolatileProprietaryTradingToFile(String sheetName, String duration, List<ProprietaryTradingStatisticEntity> orderList, List<ProprietaryTradingStatisticEntity> updatedList) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);

            updateCellString(workbook, row, 2, duration);

            for (int i = 0; i < updatedList.size(); i++) {
                String symbol = updatedList.get(i).getSymbol();
                int updatedClx = i+3;
                OptionalInt optionalIndex = IntStream.range(0, orderList.size())
                        .filter(j -> orderList.get(j).getSymbol().equals(symbol))
                        .findFirst();
                if (optionalIndex.isPresent()) {
                    updatedClx = optionalIndex.getAsInt() + 3;
                }
                updateCellString(workbook, row, updatedClx, symbol);
                log.info("Cap nhat du lieu cua ma ck {} vao cot {}.",symbol, updatedClx);
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

            updateCellString(workbook, row, getExcelColumnIndex(FSConstant.FS_QUARTER_COL_IDX), balanceSheet.getQuarter());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_NET_REVENUE_COL_IDX), incomeSheet.getNetRevenue());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_COGS_COL_IDX), incomeSheet.getCogs());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_GROSS_PROFIT_COL_IDX), incomeSheet.getGrossProfit());
            updateCellLong(workbook, row, getExcelColumnIndex(FSConstant.FS_SELLING_EXPENSES_COL_IDX), incomeSheet.getSellingExpenses());

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
                    updateCellString(workbook, row, getExcelColumnIndex(FSConstant.FS_QUARTER_COL_IDX), balanceSheets.get(i).getQuarter());
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
            }
            else{
                log.warn("Du lieu trong balance sheet, income sheet, cash flow ko khop");
                throw new RuntimeException("Du lieu trong balance sheet, income sheet, cash flow ko khop");
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

    public static void updateCellString(Workbook workbook, Row row, int columnIndex, String value) {
        Cell cell = row.getCell(columnIndex - 1);
        if (cell == null) {
            cell = row.createCell(columnIndex - 1);
        }

        CellStyle cellstyle = workbook.createCellStyle();
        cellstyle.setAlignment(HorizontalAlignment.RIGHT);

        cell.setCellValue(value);
        cell.setCellStyle(cellstyle);
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

    public static void highlightOrderBook(Workbook workbook, Row row, int colIndex, boolean color){
        Cell cell = row.getCell(colIndex-1);
        short backgroundIdx = color ? IndexedColors.GREEN.getIndex() : IndexedColors.RED.getIndex();

        CellStyle cellstyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(backgroundIdx);
        cellstyle.setFont(font);
        cell.setCellStyle(cellstyle);
    }

    public void writeDailyTopTradeToFile(String sheetName, List<?> tradeList, boolean isTNV, String tradingDate) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, 1);
            Row row = sheet.getRow(1);

            updateCellDate(workbook, row, 1, tradingDate);

            if(isTNV){
                // write tnv buy
                for (int i = 0; i < tradeList.size(); i++) {
                    if (tradeList.get(i) instanceof ForeignTradingEntity) {
                        ForeignTradingEntity entity = (ForeignTradingEntity) tradeList.get(i);
                        updateCellDouble(workbook, row, i+2, entity.getTotalNetValue(), false);
                    }

                    if (tradeList.get(i) instanceof ProprietaryTradingEntity) {
                        ProprietaryTradingEntity entity = (ProprietaryTradingEntity) tradeList.get(i);
                        updateCellDouble(workbook, row, i+2, entity.getTotalNetValue(), false);
                    }
                }

            }else {
                for (int i = 0; i < tradeList.size(); i++) {
                    if (tradeList.get(i) instanceof ForeignTradingEntity entity) {
                        updateCellString(workbook, row, i+2, entity.getSymbol());
                    }

                    if (tradeList.get(i) instanceof ProprietaryTradingEntity entity) {
                        updateCellString(workbook, row, i+2, entity.getSymbol());
                    }
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

    public void highlightTopTrade(String sheetName, List<?> tradeList) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            Row rowMCK = sheet.getRow(1);

            // merge trading_date
            sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 0));

            for (int i = 0; i < tradeList.size(); i++) {
                CellStyle cellstyle = workbook.createCellStyle();
                cellstyle.setAlignment(HorizontalAlignment.RIGHT);

                Cell buyCell = rowMCK.getCell(i+1);
                buyCell.setCellStyle(cellstyle);
            }

            Row rowTNV = sheet.getRow(2);

            for (int i = 0; i < tradeList.size(); i++) {
                if (tradeList.get(i) instanceof ForeignTradingEntity) {
                    ForeignTradingEntity entity = (ForeignTradingEntity) tradeList.get(i);
                    if(entity.isBiggestATH()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.VIOLET.getIndex());
                    } else if(entity.isBiggest6M()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.GREEN.getIndex());
                    }

                    if(entity.isSmallestATH()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.TEAL.getIndex());
                    } else if(entity.isSmallest6M()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.RED.getIndex());
                    }
                }

                if (tradeList.get(i) instanceof ProprietaryTradingEntity) {
                    ProprietaryTradingEntity entity = (ProprietaryTradingEntity) tradeList.get(i);
                    if(entity.isBiggestATH()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.VIOLET.getIndex());
                    } else if(entity.isBiggest6M()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.GREEN.getIndex());
                    }

                    if(entity.isSmallestATH()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.TEAL.getIndex());
                    } else if(entity.isSmallest6M()){
                        Cell cell = rowTNV.getCell(i+1);
                        highLightNumber(workbook, cell, IndexedColors.RED.getIndex());
                    }
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

    public void mergeCell(String sheetName, int startRow, int endRow, int startCol, int endCol) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);

            sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, startCol, endCol));

            log.info("Cap nhat du lieu vao file Excel thanh cong.");
            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }
    }

    public void writeTopWeekTradeToFile(String sheetName, List<?> tradeList, boolean isTNV, String tradingWeek) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);

            Row symbolRow = sheet.getRow(1);
            Row tnvRow = sheet.getRow(2);

            updateCellDate(workbook, symbolRow, 14, tradingWeek);
            updateCellDate(workbook, tnvRow, 14, tradingWeek);

            if(isTNV) {
                for (int i = 0; i < tradeList.size(); i++) {
                    if (tradeList.get(i) instanceof SymbolTotalNetValueDTO entity)
                        updateCellDouble(workbook, tnvRow, i+15, entity.getTotalNetValueSum(), false);
                }
            } else {
                for (int i = 0; i < tradeList.size(); i++) {
                    if (tradeList.get(i) instanceof SymbolTotalNetValueDTO entity)
                        updateCellString(workbook, symbolRow, i+15, entity.getSymbol());
                }
            }


            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }
    }

    void highLightNumber(Workbook workbook, Cell cell, short color){
        CellStyle cellstyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(color);
        cellstyle.setFont(font);
        DataFormat format = workbook.createDataFormat();
        cellstyle.setDataFormat(format.getFormat("#,##0"));
        cell.setCellStyle(cellstyle);
    }

}
