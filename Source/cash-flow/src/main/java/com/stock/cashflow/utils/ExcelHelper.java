package com.stock.cashflow.utils;

import com.stock.cashflow.constants.StockContants;
import com.stock.cashflow.dto.*;
import com.stock.cashflow.exception.BadRequestException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ExcelHelper {

    private static final Logger log = LoggerFactory.getLogger(ExcelHelper.class);

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

                int buyQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.FOREIGN_BUY_QUANTITY_COLUMN_INDEX));
                int sellQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.FOREIGN_SELL_FOREIGN_QUANTITY_COLUMN_INDEX));
                int totalForeignQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.FOREIGN_TOTAL_NET_FOREIGN_QUANTITY_COLUMN_INDEX));
                int totalForeignValueColumnIndex = Integer.parseInt(env.getProperty(StockContants.FOREIGN_TOTAL_NET_FOREIGN_VALUE_COLUMN_INDEX));

                int totalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_TOTAL_VOLUME_COLUMN_INDEX));

                double buyForeignQuantity = sym.getBuyForeignQuantity();
                updateCellValue(workbook, row, buyQuantityColumnIndex, buyForeignQuantity, false);

                double sellForeignQuantity = sym.getSellForeignQuantity();
                updateCellValue(workbook, row, sellQuantityColumnIndex, sellForeignQuantity, false);

                double totalForeignQuantity = sym.getBuyForeignQuantity() - sym.getSellForeignQuantity();
                updateCellValue(workbook, row, totalForeignQuantityColumnIndex, totalForeignQuantity, false);

                double totalForeignValue = sym.getBuyForeignValue() - sym.getSellForeignValue();
                updateCellValue(workbook, row, totalForeignValueColumnIndex, totalForeignValue, false);

                double totalVolume = sym.getTotalVolume();
                updateCellValue(workbook, row, totalVolumeColumnIndex, totalVolume, false);
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
                if(!sheetName.contains(StockContants.STATISTIC_INGORE_SHEET)){
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

    private static List<String> getSheetNames(Workbook workbook) {
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

                int totalBuyTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.PROPRIETARY_BUY_QUANTITY_COLUMN_INDEX));
                int totalSellTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.PROPRIETARY_SELL_QUANTITY_COLUMN_INDEX));
                int totalNetTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.PROPRIETARY_TOTAL_NET_QUANTITY_COLUMN_INDEX));
                int totalNetTradeValueColumnIndex = Integer.parseInt(env.getProperty(StockContants.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX));

                double totalBuyTradeValue = trade.getTotalBuyTradeValue();
                double totalSellTradeValue = trade.getTotalSellTradeValue();
                double totalBuyTradeVolume = trade.getTotalBuyTradeVolume();
                double totalSellTradeVolume = trade.getTotalSellTradeVolume();

                double totalNetTradeVolume = trade.getTotalNetBuyTradeVolume();
                double totalNetTradeValue = trade.getTotalNetBuyTradeValue();

                updateCellValue(workbook, row, totalBuyTradeVolumeColumnIndex, totalBuyTradeVolume, false);

                updateCellValue(workbook, row, totalSellTradeVolumeColumnIndex, totalSellTradeVolume, false);

                updateCellValue(workbook, row, totalNetTradeVolumeColumnIndex, totalNetTradeVolume, false);

                updateCellValue(workbook, row, totalNetTradeValueColumnIndex, totalNetTradeValue, false);

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

                int totalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_TOTAL_VOLUME_COLUMN_INDEX));
                int percentagePOnTotalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_PERCENTAGE_OF_PV_ON_TV_COLUMN_INDEX));
                int percentageFOnTotalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_PERCENTAGE_OF_FV_ON_TV_COLUMN_INDEX));
                int percentagePriceChangeColumnIndex = Integer.parseInt(env.getProperty(StockContants.STATISTIC_PRICE_CHANGE_COLUMN_INDEX));
                int proprietaryTotalBuyTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.PROPRIETARY_BUY_QUANTITY_COLUMN_INDEX));
                int proprietaryTotalSellTradeVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.PROPRIETARY_SELL_QUANTITY_COLUMN_INDEX));
                int foreignBuyQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.FOREIGN_BUY_QUANTITY_COLUMN_INDEX));
                int foreignSellQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.FOREIGN_SELL_FOREIGN_QUANTITY_COLUMN_INDEX));

                double totalVolume = row.getCell(totalVolumeColumnIndex - 1).getNumericCellValue();

                // khoi ngoai
                double foreignTotalBuyTradeVolume = row.getCell(foreignBuyQuantityColumnIndex - 1).getNumericCellValue();
                double foreignTotalSellTradeVolume = row.getCell(foreignSellQuantityColumnIndex - 1).getNumericCellValue();
                updateCellValue(workbook, row, percentageFOnTotalVolumeColumnIndex, (foreignTotalBuyTradeVolume + foreignTotalSellTradeVolume)/totalVolume, true);

                // tu doanh
                double proprietaryTotalBuyTradeVolume = row.getCell(proprietaryTotalBuyTradeVolumeColumnIndex - 1).getNumericCellValue();
                double proprietaryTotalSellTradeVolume = row.getCell(proprietaryTotalSellTradeVolumeColumnIndex - 1).getNumericCellValue();
                updateCellValue(workbook, row, percentagePOnTotalVolumeColumnIndex, (proprietaryTotalBuyTradeVolume + proprietaryTotalSellTradeVolume)/totalVolume, true);

                double percentPriceChange = Double.parseDouble(price.getPerPriceChange());
                updateCellValue(workbook, row, percentagePriceChangeColumnIndex, percentPriceChange, true);

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

            int buyOrderColumnIndex = Integer.parseInt(env.getProperty(StockContants.INTRADAY_BUY_ORDER_COLUMN_INDEX));
            int sellOrderColumnIndex = Integer.parseInt(env.getProperty(StockContants.INTRADAY_SELL_ORDER_COLUMN_INDEX));
            int buyVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.INTRADAY_BUY_VOLUME_COLUMN_INDEX));
            int sellVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.INTRADAY_SELL_VOLUME_COLUMN_INDEX));

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

            updateCellValue(workbook, row, buyOrderColumnIndex, buyOrder, false);
            updateCellValue(workbook, row, sellOrderColumnIndex, sellOrder, false);
            updateCellValue(workbook, row, buyVolumeColumnIndex, buyVolume, false);
            updateCellValue(workbook, row, sellVolumeColumnIndex, sellVolume, false);

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

    public void updateDerivatives(String filePath, String sheetName, int columnIndex, int startRowIndex, int endRowIndex, Symbol[] data) {
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

                int buyQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.DERIVATIVES_FOREIGN_BUY_QUANTITY_COLUMN_INDEX));
                int sellQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.DERIVATIVES_FOREIGN_SELL_QUANTITY_COLUMN_INDEX));
                int totalForeignQuantityColumnIndex = Integer.parseInt(env.getProperty(StockContants.DERIVATIVES_FOREIGN_TOTAL_NET_QUANTITY_COLUMN_INDEX));
                int totalForeignValueColumnIndex = Integer.parseInt(env.getProperty(StockContants.DERIVATIVES_FOREIGN_TOTAL_NET_VALUE_COLUMN_INDEX));
                int totalVolumeColumnIndex = Integer.parseInt(env.getProperty(StockContants.DERIVATIVES_TOTAL_VOLUME_COLUMN_INDEX));

                double buyForeignQuantity = sym.getBuyForeignQuantity();
                updateCellValue(workbook, row, buyQuantityColumnIndex, buyForeignQuantity, false);

                double sellForeignQuantity = sym.getSellForeignQuantity();
                updateCellValue(workbook, row, sellQuantityColumnIndex, sellForeignQuantity, false);

                double totalForeignQuantity = sym.getBuyForeignQuantity() - sym.getSellForeignQuantity();
                updateCellValue(workbook, row, totalForeignQuantityColumnIndex, totalForeignQuantity, false);

                double totalForeignValue = sym.getBuyForeignValue() - sym.getSellForeignValue();
                updateCellValue(workbook, row, totalForeignValueColumnIndex, totalForeignValue, false);

                double totalVolume = sym.getTotalVolume();
                updateCellValue(workbook, row, totalVolumeColumnIndex, totalVolume, false);
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

    public static void updateCellValue(Workbook workbook, Row row, int columnIndex, Double value, boolean isPercentage) {
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

        cell.setCellValue(value);
    }

}
