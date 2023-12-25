package com.stock.cashflow.utils;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.exception.BadRequestException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ExcelHelper {

    private static final Logger log = LoggerFactory.getLogger(ExcelHelper.class);

    @Value("${statistics.file.path}")
    private String statisticFile;

    @Value("${statistics.insert.new.row.index}")
    private int beginRowIndex;

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
                            String cellValue = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                            if (targetValue.equals(cellValue)) {
                                return rowIndex;
                            }
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

    public void updateForeignCell(String filePath, String sheetName, int columnIndex, int startRowIndex, int endRowIndex, Symbol[] data) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);

            Arrays.stream(data).forEach(sym -> {

                // Get or create the row
                Row row = null;
                for (int rowIndex = startRowIndex - 1; rowIndex <= endRowIndex  ; rowIndex++) {
                    Row loopRow = sheet.getRow(rowIndex);

                    if (loopRow != null) {
                        Cell cell = loopRow.getCell(columnIndex - 1);

                        if (cell != null) {
                            try {
                                String cellValue = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                                Instant instant = sym.getDate().toInstant();
                                LocalDate lastDateLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                                String dateValue = lastDateLocalDate.toString();
                                if (dateValue.equals(cellValue)) {
                                    row = sheet.getRow(rowIndex);
                                    log.info("Cap nhat du lieu ngay {} vao dong {}", cellValue, rowIndex);
                                    break;
                                }
                            } catch (IllegalStateException ex) {
                                log.error("Format date khong dung o cot date trong file Excel. {}", cell.getStringCellValue());
                                throw new BadRequestException("Format date khong dung o cot date trong file Excel.");
                            }
                        }
                    }
                }

//                int buyQuantityColumnIndex = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_BUY_QUANTITY_COLUMN_INDEX));
//                int sellQuantityColumnIndex = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_SELL_FOREIGN_QUANTITY_COLUMN_INDEX));
//                int totalForeignQuantityColumnIndex = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_TOTAL_NET_FOREIGN_QUANTITY_COLUMN_INDEX));
//                int totalForeignValueColumnIndex = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_TOTAL_NET_FOREIGN_VALUE_COLUMN_INDEX));
//
//                int totalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.STATISTIC_TOTAL_VOLUME_COLUMN_INDEX));
//
//                double buyForeignQuantity = sym.getBuyForeignQuantity();
//                updateCellDouble(workbook, row, buyQuantityColumnIndex, buyForeignQuantity, false);
//
//                double sellForeignQuantity = sym.getSellForeignQuantity();
//                updateCellDouble(workbook, row, sellQuantityColumnIndex, sellForeignQuantity, false);
//
//                double totalForeignQuantity = sym.getBuyForeignQuantity() - sym.getSellForeignQuantity();
//                updateCellDouble(workbook, row, totalForeignQuantityColumnIndex, totalForeignQuantity, false);
//
//                double totalForeignValue = sym.getBuyForeignValue() - sym.getSellForeignValue();
//                updateCellDouble(workbook, row, totalForeignValueColumnIndex, totalForeignValue, false);
//
//                double totalVolume = sym.getTotalVolume();
//                updateCellDouble(workbook, row, totalVolumeColumnIndex, totalVolume, false);
            });

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh cap nhat file. {}", filePath);
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

    public void updateProprietaryCell(String filePath, int columnIndex, int startRowIndex, int endRowIndex, List<ProprietaryTrade> proprietaryTrades) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            proprietaryTrades.forEach(trade -> {
                String sheetName = trade.getOrganCode().concat("-Q4");
                Sheet sheet = workbook.getSheet(sheetName);
                // Get or create the row
                Row row = null;
                for (int rowIndex = startRowIndex - 1; rowIndex <= endRowIndex  ; rowIndex++) {
                    Row loopRow = sheet.getRow(rowIndex);

                    if (loopRow != null) {
                        Cell cell = loopRow.getCell(columnIndex - 1);

                        if (cell != null) {
                            try {
                                String cellValue = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                                Instant instant = trade.getToDate().toInstant();
                                LocalDate lastDateLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                                String dateValue = lastDateLocalDate.toString();
                                if (dateValue.equals(cellValue)) {
                                    row = sheet.getRow(rowIndex);
                                    log.info("Cap nhat du lieu ngay {} vao dong {}", cellValue, rowIndex);
                                    break;
                                }
                            } catch (IllegalStateException ex) {
                                log.error("Format date khong dung o cot date trong file Excel. {}", cell.getStringCellValue());
                                throw new BadRequestException("Format date khong dung o cot date trong file Excel.");
                            }
                        }
                    }
                }

//                int totalBuyTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_BUY_QUANTITY_COLUMN_INDEX));
//                int totalSellTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_SELL_QUANTITY_COLUMN_INDEX));
//                int totalNetTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_TOTAL_NET_QUANTITY_COLUMN_INDEX));
//                int totalNetTradeValueColumnIndex = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX));
//
//                double totalBuyTradeValue = trade.getTotalBuyTradeValue();
//                double totalSellTradeValue = trade.getTotalSellTradeValue();
//                double totalBuyTradeVolume = trade.getTotalBuyTradeVolume();
//                double totalSellTradeVolume = trade.getTotalSellTradeVolume();
//
//                double totalNetTradeVolume = trade.getTotalNetBuyTradeVolume();
//                double totalNetTradeValue = trade.getTotalNetBuyTradeValue();
//
//                updateCellDouble(workbook, row, totalBuyTradeVolumeColumnIndex, totalBuyTradeVolume, false);
//
//                updateCellDouble(workbook, row, totalSellTradeVolumeColumnIndex, totalSellTradeVolume, false);
//
//                updateCellDouble(workbook, row, totalNetTradeVolumeColumnIndex, totalNetTradeVolume, false);
//
//                updateCellDouble(workbook, row, totalNetTradeValueColumnIndex, totalNetTradeValue, false);

            });

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh cap nhat file. {}", filePath);
        }
    }

    public void updatePercentageCell(String filePath, String sheetName, int columnIndex, int startRowIndex, int endRowIndex, List<StockPrice> prices) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);

            prices.forEach(price -> {
                Row row = null;
                for (int rowIndex = startRowIndex - 1; rowIndex <= endRowIndex  ; rowIndex++) {
                    Row loopRow = sheet.getRow(rowIndex);

                    if (loopRow != null) {
                        Cell cell = loopRow.getCell(columnIndex - 1);

                        if (cell != null) {
                            try {
                                String cellValue = cell.getLocalDateTimeCellValue().toLocalDate().toString();

                                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                LocalDateTime dateTime = LocalDateTime.parse(price.getTradingDate(), inputFormatter);
                                String dateValue = dateTime.format(outputFormatter);
                                if (dateValue.equals(cellValue)) {
                                    row = sheet.getRow(rowIndex);
                                    log.info("Cap nhat du lieu ngay {} vao dong {}", cellValue, rowIndex);
                                    break;
                                }
                            } catch (IllegalStateException ex) {
                                log.error("Format date khong dung o cot date trong file Excel. {}", cell.getStringCellValue());
                                throw new BadRequestException("Format date khong dung o cot date trong file Excel.");
                            }
                        }
                    }
                }

//                int totalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.STATISTIC_TOTAL_VOLUME_COLUMN_INDEX));
//                int percentagePOnTotalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.STATISTIC_PERCENTAGE_OF_PV_ON_TV_COLUMN_INDEX));
//                int percentageFOnTotalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.STATISTIC_PERCENTAGE_OF_FV_ON_TV_COLUMN_INDEX));
//                int percentagePriceChangeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.STATISTIC_PRICE_CHANGE_COLUMN_INDEX));
//                int proprietaryTotalBuyTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_BUY_QUANTITY_COLUMN_INDEX));
//                int proprietaryTotalSellTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_SELL_QUANTITY_COLUMN_INDEX));
//                int foreignBuyQuantityColumnIndex = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_BUY_QUANTITY_COLUMN_INDEX));
//                int foreignSellQuantityColumnIndex = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_SELL_FOREIGN_QUANTITY_COLUMN_INDEX));
//
//                double totalVolume = row.getCell(totalVolumeColumnIndex - 1).getNumericCellValue();
//
//                // khoi ngoai
//                double foreignTotalBuyTradeVolume = row.getCell(foreignBuyQuantityColumnIndex - 1).getNumericCellValue();
//                double foreignTotalSellTradeVolume = row.getCell(foreignSellQuantityColumnIndex - 1).getNumericCellValue();
//                updateCellDouble(workbook, row, percentageFOnTotalVolumeColumnIndex, (foreignTotalBuyTradeVolume + foreignTotalSellTradeVolume)/totalVolume, true);
//
//                // tu doanh
//                double proprietaryTotalBuyTradeVolume = row.getCell(proprietaryTotalBuyTradeVolumeColumnIndex - 1).getNumericCellValue();
//                double proprietaryTotalSellTradeVolume = row.getCell(proprietaryTotalSellTradeVolumeColumnIndex - 1).getNumericCellValue();
//                updateCellDouble(workbook, row, percentagePOnTotalVolumeColumnIndex, (proprietaryTotalBuyTradeVolume + proprietaryTotalSellTradeVolume)/totalVolume, true);
//
//                double percentPriceChange = Double.parseDouble(price.getPerPriceChange());
//                updateCellDouble(workbook, row, percentagePriceChangeColumnIndex, percentPriceChange, true);

            });


            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh cap nhat file. {}", filePath);
        }
    }

    public void updateIntradayTrading(String filePath, String sheetName, int rowIndex, List<Intraday> intradayData) {

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            // Get or create the row
            Row row = sheet.getRow(rowIndex);

            int buyOrderColumnIndex = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX));
            int sellOrderColumnIndex = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX));
            int buyVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX));
            int sellVolumeColumnIndex = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX));

            double buyOrder = 0;
            double sellOrder = 0;
            double buyVolume = 0;
            double sellVolume = 0;

            for (Intraday item : intradayData) {
                String side = item.getSide();
                if(side.equals("PS")){
                    buyOrder++;
                    buyVolume += item.getLastVol();
                }else if(side.equals("PB")){
                    sellOrder++;
                    sellVolume += item.getLastVol();
                }
            }

            updateCellDouble(workbook, row, buyOrderColumnIndex, buyOrder, false);
            updateCellDouble(workbook, row, sellOrderColumnIndex, sellOrder, false);
            updateCellDouble(workbook, row, buyVolumeColumnIndex, buyVolume, false);
            updateCellDouble(workbook, row, sellVolumeColumnIndex, sellVolume, false);

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh cap nhat file. {}", filePath);
        }
    }

    public void writeDataOfSymbolToFile(String sheetName, StockStatisticsDTO data) {
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            insertNewRow(sheet, beginRowIndex);
            Row row = sheet.getRow(beginRowIndex);

            int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));

            int foreignBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_BUY_VOL_COLUMN_INDEX));
            int foreignSellVolIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_SELL_VOL_COLUMN_INDEX));
            int foreignNetVolIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_TOTAL_NET_VOL_COLUMN_INDEX));
            int foreignNetValIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_TOTAL_NET_VAL_COLUMN_INDEX));

            int proprietaryBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX));
            int proprietarySellVolIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX));
            int proprietaryNetVolIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX));
            int proprietaryNetValIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX));

            int orderBuyIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX));
            int orderSellIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX));
            int orderBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX));
            int orderSellVolIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX));

            int totalVolumeIdx = Integer.parseInt(env.getProperty(StockConstant.TOTAL_VOL_COLUMN_INDEX));
            int percenChangeIdx = Integer.parseInt(env.getProperty(StockConstant.PERCENTAGE_CHANGE_COLUMN_INDEX));

            double foreignBuyVolume = data.getForeignBuyVolume();
            double foreignSellVolume = data.getForeignSellVolume();
            double foreignBuyValue = data.getForeignBuyValue();
            double foreignSellValue = data.getForeignSellValue();

            if(!Objects.isNull(data.getProprietaryBuyVolume())){
                double proprietaryBuyVolume = data.getProprietaryBuyVolume();
                double proprietarySellVolume = data.getProprietarySellVolume();
                double proprietaryBuyValue = data.getProprietaryBuyValue();
                double proprietarySellValue = data.getProprietarySellValue();
                updateCellDouble(workbook, row, proprietaryBuyVolIdx, proprietaryBuyVolume, false);
                updateCellDouble(workbook, row, proprietarySellVolIdx, proprietarySellVolume, false);
                updateCellDouble(workbook, row, proprietaryNetVolIdx, proprietaryBuyVolume - proprietarySellVolume, false);
                updateCellDouble(workbook, row, proprietaryNetValIdx, proprietaryBuyValue - proprietarySellValue, false);
            }

            if(!Objects.isNull(data.getBuyOrder())){
                double buyOrder = data.getBuyOrder();
                double sellOrder = data.getSellOrder();
                double buyOrderVol = data.getBuyOrderVolume();
                double sellOrderVol = data.getSellOrderVolume();
                updateCellDouble(workbook, row, orderBuyIdx, buyOrder, false);
                updateCellDouble(workbook, row, orderSellIdx, sellOrder, false);
                updateCellDouble(workbook, row, orderBuyVolIdx, buyOrderVol, false);
                updateCellDouble(workbook, row, orderSellVolIdx, sellOrderVol, false);
            }

            double totalVolume = data.getTotalVolume();
            String percenChange = data.getPercentageChange().replace("%", "");


            updateCellDate(workbook, row, tradingDateIdx, data.getTradingDate());
            updateCellDouble(workbook, row, foreignBuyVolIdx, foreignBuyVolume, false);
            updateCellDouble(workbook, row, foreignSellVolIdx, foreignSellVolume, false);
            updateCellDouble(workbook, row, foreignNetVolIdx, foreignBuyVolume - foreignSellVolume, false);
            updateCellDouble(workbook, row, foreignNetValIdx, foreignBuyValue - foreignSellValue, false);

            updateCellDouble(workbook, row, totalVolumeIdx, totalVolume, false);
            updateCellDouble(workbook, row, percenChangeIdx, Double.parseDouble(percenChange)/100, true);

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

    public static void insertNewRow(Sheet sheet, int rowIndex) {
        // Shift existing rows down to make space for the new row
        sheet.shiftRows(rowIndex, sheet.getLastRowNum(), 1, false, true);
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
