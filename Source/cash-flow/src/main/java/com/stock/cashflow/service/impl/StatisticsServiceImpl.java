package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.TradingStatistics;
import com.stock.cashflow.persistence.entity.*;
import com.stock.cashflow.persistence.repository.*;
import com.stock.cashflow.service.StatisticsService;
import com.stock.cashflow.utils.DateHelper;
import com.stock.cashflow.utils.ExcelHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private final Map<String, Integer> columnIdx =
            Map.of(
//                    "trading-date", 2,
//                    "foreign-buy-volume", 3,
//                    "foreign-sell-volume", 4,
//                    "foreign-total-volume", 5,
//                    "foreign-total-value", 6,
                    "proprietary-buy-volume", 7,
                    "proprietary-sell-volume", 8,
                    "proprietary-total-volume", 9,
                    "proprietary-total-value", 10,
                    "intraday-buy-order", 12,
                    "intraday-sell-order", 13,
                    "intraday-big-buy-order", 14,
                    "intraday-big-sell-order", 15,
                    "intraday-buy-volume", 16
//                    "percentage-change", 17,
//                    "total-volume", 18
            );


    private final ExcelHelper excelHelper;
    private final ProprietaryTradingRepository proprietaryTradingRepository;
    private final ForeignTradingRepository foreignTradingRepository;
    private final IntradayOrderRepository intradayOrderRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DerivativesTradingRepository derivativesTradingRepository;
    private final OrderBookRepository orderBookRepository;
    private final Environment env;

    @Value("${statistics.file.path}")
    private String statisticFile;

    @Value("${derivatives.file.path}")
    private String derivativesFile;

    @Value("${statistics.insert.new.row.index}")
    private int statisticInsertRow;

    @Value("${derivatives.insert.row.index}")
    private int derivativesInsertRow;

    @Autowired
    public StatisticsServiceImpl(ExcelHelper excelHelper,
                                 ProprietaryTradingRepository proprietaryTradingRepository,
                                 StockPriceRepository stockPriceRepository,
                                 ForeignTradingRepository foreignTradingRepository,
                                 IntradayOrderRepository intradayOrderRepository,
                                 DerivativesTradingRepository derivativesTradingRepository,
                                 OrderBookRepository orderBookRepository,
                                 Environment env
                                 ){
        this.excelHelper = excelHelper;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
        this.intradayOrderRepository = intradayOrderRepository;
        this.foreignTradingRepository = foreignTradingRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.derivativesTradingRepository = derivativesTradingRepository;
        this.orderBookRepository = orderBookRepository;
        this.env = env;
    }


    @Override
    public void writeSpecificDate(String symbol, String tradingDate) {
        String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);
        ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
//        IntradayOrderEntity intradayOrderEntity = intradayOrderRepository.findIntradayOrderEntitiesBySymbolAndHashDate(symbol, hashDate);
        OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndHashDate(symbol, hashDate);
        ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
        StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesBySymbolAndHashDate(symbol, hashDate);

        TradingStatistics data = new TradingStatistics();
        data.setTradingDate(foreignTradingEntity.getTradingDate().toString());
        data.setForeignBuyValue(foreignTradingEntity.getBuyValue());
        data.setForeignSellValue(foreignTradingEntity.getSellValue());
        data.setForeignBuyVolume(foreignTradingEntity.getBuyVolume());
        data.setForeignSellVolume(foreignTradingEntity.getSellVolume());

        if(!Objects.isNull(proprietaryTradingEntity)){
            data.setProprietaryBuyVolume(proprietaryTradingEntity.getBuyVolume());
            data.setProprietarySellVolume(proprietaryTradingEntity.getSellVolume());
            data.setProprietaryBuyValue(proprietaryTradingEntity.getBuyValue());
            data.setProprietarySellValue(proprietaryTradingEntity.getSellValue());
        }

        if(!Objects.isNull(orderBookEntity)){
            data.setBuyOrder(orderBookEntity.getBuyOrder());
            data.setSellOrder(orderBookEntity.getSellOrder());
            data.setBigBuyOrder(orderBookEntity.getBigBuyOrder());
            data.setBigSellOrder(orderBookEntity.getBigSellOrder());
            data.setBuyOrderVolume(orderBookEntity.getBuyVolume());
            data.setSellOrderVolume(orderBookEntity.getSellVolume());
        }

        data.setTotalVolume(stockPriceEntity.getTotalVolume());
        data.setPercentageChange(stockPriceEntity.getPercentageChange());
        data.setPriceRange(stockPriceEntity.getPriceRange());

        log.info("Ghi du lieu cho ma {}", symbol);
        excelHelper.writeIntradayTradingStatisticsToFile(symbol, data);

    }


    @Override
    public void writeDateToDate(String symbol, String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        ArrayList<String> tradingDates = daysInRange(startDate, endDate);

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
        int orderBigBuyIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BIG_BUY_ORDER_COLUMN_INDEX));
        int orderBigSellIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BIG_SELL_ORDER_COLUMN_INDEX));
        int orderBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX));
        int orderSellVolIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX));

        int totalVolumeIdx = Integer.parseInt(env.getProperty(StockConstant.TOTAL_VOL_COLUMN_INDEX));
        int percenChangeIdx = Integer.parseInt(env.getProperty(StockConstant.PERCENTAGE_CHANGE_COLUMN_INDEX));

        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (String tradingDate : tradingDates) {
                String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
                ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesBySymbolAndHashDate(symbol, hashDate);

                // xu ly cho truong hop nghi le
                if (Objects.isNull(foreignTradingEntity)) {
                    log.info("Khong tim thay du lieu giao dich trong ngay {}", tradingDate);
                    continue;
                }

                OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndHashDate(symbol, hashDate);
//                IntradayOrderEntity intradayOrderEntity = intradayOrderRepository.findIntradayOrderEntitiesBySymbolAndHashDate(symbol, hashDate);
                ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
                StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesBySymbolAndHashDate(symbol, hashDate);

                log.info("Ghi du lieu cua ma {} cho ngay {}", symbol, tradingDate);

                ZipSecureFile.setMinInflateRatio(0);
                Sheet sheet = workbook.getSheet(symbol);
                excelHelper.insertNewRow(sheet, statisticInsertRow);
                Row row = sheet.getRow(statisticInsertRow);

                if (!Objects.isNull(proprietaryTradingEntity)) {
                    double proprietaryBuyVolume = proprietaryTradingEntity.getBuyVolume();
                    double proprietarySellVolume = proprietaryTradingEntity.getSellVolume();
                    double proprietaryBuyValue = proprietaryTradingEntity.getBuyValue();
                    double proprietarySellValue = proprietaryTradingEntity.getSellValue();
                    excelHelper.updateCellDouble(workbook, row, proprietaryBuyVolIdx, proprietaryBuyVolume, false);
                    excelHelper.updateCellDouble(workbook, row, proprietarySellVolIdx, proprietarySellVolume, false);
                    excelHelper.updateCellDouble(workbook, row, proprietaryNetVolIdx, proprietaryBuyVolume - proprietarySellVolume, false);
                    excelHelper.updateCellDouble(workbook, row, proprietaryNetValIdx, proprietaryBuyValue - proprietarySellValue, false);
                }

                if (!Objects.isNull(orderBookEntity)) {
                    double buyOrder = orderBookEntity.getBuyOrder();
                    double sellOrder = orderBookEntity.getSellOrder();
                    double bigBuyOrder = orderBookEntity.getBigBuyOrder();
                    double bigSellOrder = orderBookEntity.getBigSellOrder();
                    double buyOrderVol = orderBookEntity.getBuyVolume();
                    double sellOrderVol = orderBookEntity.getSellVolume();
                    excelHelper.updateCellDouble(workbook, row, orderBuyIdx, buyOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderSellIdx, sellOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderBigBuyIdx, bigBuyOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderBigSellIdx, bigSellOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderBuyVolIdx, buyOrderVol, false);
                    excelHelper.updateCellDouble(workbook, row, orderSellVolIdx, sellOrderVol, false);
                }

                double totalVolume = stockPriceEntity.getTotalVolume();
                String percenChange = stockPriceEntity.getPercentageChange().replace("%", "");

                excelHelper.updateCellDate(workbook, row, tradingDateIdx, foreignTradingEntity.getTradingDate().toString());
                excelHelper.updateCellDouble(workbook, row, foreignBuyVolIdx, foreignTradingEntity.getBuyVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignSellVolIdx, foreignTradingEntity.getSellVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignNetVolIdx, foreignTradingEntity.getBuyVolume() - foreignTradingEntity.getSellVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignNetValIdx, foreignTradingEntity.getBuyValue() - foreignTradingEntity.getSellValue(), false);
                excelHelper.updateCellDouble(workbook, row, totalVolumeIdx, totalVolume, false);
                excelHelper.updateCellDouble(workbook, row, percenChangeIdx, Double.parseDouble(percenChange) / 100, true);

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
        log.info("Ghi du lieu giao dich tu ngay {} den ngay {} vao file thanh cong", start, end);
    }

    @Override
    public void writeAllForSpecificDate(String tradingDate) {
        String[] symbols = SymbolConstant.SYMBOLS;

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));

            List<String> sheetNames = excelHelper.getSheetNames(workbook);
            for (String symbol : sheetNames) {
                if(Arrays.asList(symbols).contains(symbol)){
                    log.info("Luu du lieu vao Sheet name {}", symbol);
                    String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);
                    ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
//                    IntradayOrderEntity intradayOrderEntity = intradayOrderRepository.findIntradayOrderEntitiesBySymbolAndHashDate(symbol, hashDate);
                    OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndHashDate(symbol, hashDate);
                    ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
                    StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesBySymbolAndHashDate(symbol, hashDate);

                    log.info("Ghi du lieu cua ma {} cho ngay {}", symbol, tradingDate);

                    Sheet sheet = workbook.getSheet(symbol);
                    excelHelper.insertNewRow(sheet, statisticInsertRow);
                    Row row = sheet.getRow(statisticInsertRow);

                    if(!Objects.isNull(proprietaryTradingEntity)){
                        double proprietaryBuyVolume = proprietaryTradingEntity.getBuyVolume();
                        double proprietarySellVolume = proprietaryTradingEntity.getSellVolume();
                        double proprietaryBuyValue = proprietaryTradingEntity.getBuyValue();
                        double proprietarySellValue = proprietaryTradingEntity.getSellValue();
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX), proprietaryBuyVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX), proprietarySellVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX), proprietaryBuyVolume - proprietarySellVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX), proprietaryBuyValue - proprietarySellValue, false);
                    }

                    if(!Objects.isNull(orderBookEntity)){
                        double buyOrder = orderBookEntity.getBuyOrder();
                        double sellOrder = orderBookEntity.getSellOrder();
                        double bigBuyOrder = orderBookEntity.getBigBuyOrder();
                        double bigSellOrder = orderBookEntity.getBigSellOrder();
                        double buyOrderVol = orderBookEntity.getBuyVolume();
                        double sellOrderVol = orderBookEntity.getSellVolume();
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX), buyOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX), sellOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BIG_BUY_ORDER_COLUMN_INDEX), bigBuyOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BIG_SELL_ORDER_COLUMN_INDEX), bigSellOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX), buyOrderVol, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX), sellOrderVol, false);
                    }

                    double totalVolume = stockPriceEntity.getTotalVolume();
                    String percenChange = stockPriceEntity.getPercentageChange().replace("%", "");
                    String priceRange = stockPriceEntity.getPriceRange().replace("%", "");

                    excelHelper.updateCellDate(workbook, row, tradingDateIdx, foreignTradingEntity.getTradingDate().toString());
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_BUY_VOL_COLUMN_INDEX), foreignTradingEntity.getBuyVolume(), false);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_SELL_VOL_COLUMN_INDEX), foreignTradingEntity.getSellVolume(), false);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_TOTAL_NET_VOL_COLUMN_INDEX), foreignTradingEntity.getBuyVolume() - foreignTradingEntity.getSellVolume(), false);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_TOTAL_NET_VAL_COLUMN_INDEX), foreignTradingEntity.getBuyValue() - foreignTradingEntity.getSellValue(), false);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.TOTAL_VOL_COLUMN_INDEX), totalVolume, false);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PERCENTAGE_CHANGE_COLUMN_INDEX), Double.parseDouble(percenChange)/100, true);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PRICE_RANGE_COLUMN_INDEX), Double.parseDouble(priceRange)/100, true);


                }
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", statisticFile);
        }
    }

    private int getExcelColumnIndex(String columnName){
        return Integer.parseInt(env.getProperty(columnName));
    }

    @Override
    public void writeDerivativesDateToDate(String symbol, String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        ArrayList<String> tradingDates = daysInRange(startDate, endDate);

        if(!tradingDates.isEmpty()){
            int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_TRADING_DATE_COLUMN_INDEX));
            int foreignBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOREIGN_BUY_VOLUME_COLUMN_INDEX));
            int foreignSellVolIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOREIGN_SELL_VOLUME_COLUMN_INDEX));
            int foreignNetVolIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOREIGN_NET_VOLUME_COLUMN_INDEX));
            int foreignBuyValIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOREIGN_BUY_VALUE_COLUMN_INDEX));
            int foreignSellValIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOREIGN_SELL_VALUE_COLUMN_INDEX));
            int foreignNetValIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOREIGN_NET_VALUE_COLUMN_INDEX));
            int proprietaryBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PROPRIETARY_BUY_VOLUME_COLUMN_INDEX));
            int proprietarySellVolIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PROPRIETARY_SELL_VOLUME_COLUMN_INDEX));
            int proprietaryNetVolIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PROPRIETARY_NET_VOLUME_COLUMN_INDEX));
            int proprietaryBuyValIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PROPRIETARY_BUY_VALUE_COLUMN_INDEX));
            int proprietarySellValIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PROPRIETARY_SELL_VALUE_COLUMN_INDEX));
            int proprietaryNetValIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PROPRIETARY_NET_VALUE_COLUMN_INDEX));
            int oiIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_OPEN_INTEREST_COLUMN_INDEX));
            int totalVolumeIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_TOTAL_VOLUME_COLUMN_INDEX));
            int percentFOTV = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_FOTV_COLUMN_INDEX));
            int percentPOTV = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_POTV_COLUMN_INDEX));
            int percenChangeIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_PERCENTAGE_CHANGE_COLUMN_INDEX));


            try (FileInputStream fileInputStream = new FileInputStream(derivativesFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
                for (String tradingDate : tradingDates) {
                    String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
                    DerivativesTradingEntity derivativesTrading = derivativesTradingRepository.findDerivativesTradingEntitiesByHashDate(hashDate);

                    // xu ly cho truong hop nghi le
                    if (Objects.isNull(derivativesTrading)) {
                        log.info("Khong tim thay du lieu giao dich trong ngay {}", tradingDate);
                        continue;
                    }

                    log.info("Ghi du lieu phai sinh {} cho ngay {}", symbol, tradingDate);

                    ZipSecureFile.setMinInflateRatio(0);
                    Sheet sheet = workbook.getSheet(symbol);
                    excelHelper.insertNewRow(sheet, derivativesInsertRow);
                    Row row = sheet.getRow(derivativesInsertRow);

                    String percenChange = derivativesTrading.getPercentageChange().replace("%", "");

                    excelHelper.updateCellDate(workbook, row, tradingDateIdx, derivativesTrading.getTradingDate().toString());

                    excelHelper.updateCellInt(workbook, row, foreignBuyVolIdx, derivativesTrading.getForeignBuyVolume());
                    excelHelper.updateCellInt(workbook, row, foreignSellVolIdx, derivativesTrading.getForeignSellVolume());
                    excelHelper.updateCellInt(workbook, row, foreignNetVolIdx, derivativesTrading.getForeignBuyVolume() - derivativesTrading.getForeignSellVolume());

                    excelHelper.updateCellDouble(workbook, row, foreignBuyValIdx, derivativesTrading.getForeignBuyValue(), false);
                    excelHelper.updateCellDouble(workbook, row, foreignSellValIdx, derivativesTrading.getForeignSellValue(), false);
                    excelHelper.updateCellDouble(workbook, row, foreignNetValIdx, derivativesTrading.getForeignBuyValue() - derivativesTrading.getForeignSellValue(), false);

                    excelHelper.updateCellInt(workbook, row, proprietaryBuyVolIdx, derivativesTrading.getProprietaryBuyVolume());
                    excelHelper.updateCellInt(workbook, row, proprietarySellVolIdx, derivativesTrading.getProprietarySellVolume());
                    excelHelper.updateCellInt(workbook, row, proprietaryNetVolIdx, derivativesTrading.getProprietaryBuyVolume() - derivativesTrading.getProprietarySellVolume());

                    excelHelper.updateCellDouble(workbook, row, proprietaryBuyValIdx, derivativesTrading.getProprietaryBuyValue(), false);
                    excelHelper.updateCellDouble(workbook, row, proprietarySellValIdx, derivativesTrading.getProprietarySellValue(), false);
                    excelHelper.updateCellDouble(workbook, row, proprietaryNetValIdx, derivativesTrading.getProprietaryBuyValue() - derivativesTrading.getProprietarySellValue(), false);

                    excelHelper.updateCellInt(workbook, row, oiIdx, derivativesTrading.getOpenInterest());
                    excelHelper.updateCellDouble(workbook, row, totalVolumeIdx, derivativesTrading.getTotalVolume(), false);
                    excelHelper.updateCellDouble(workbook, row, percentFOTV, (derivativesTrading.getForeignBuyVolume() + derivativesTrading.getForeignSellVolume()) / derivativesTrading.getTotalVolume(), true);
                    excelHelper.updateCellDouble(workbook, row, percentPOTV, (derivativesTrading.getProprietaryBuyVolume() + derivativesTrading.getProprietarySellVolume()) / derivativesTrading.getTotalVolume(), true);
                    excelHelper.updateCellDouble(workbook, row, percenChangeIdx, Double.parseDouble(percenChange) / 100, true);

                }

                // Save the workbook to a file
                try (FileOutputStream fileOut = new FileOutputStream(derivativesFile)) {
                    workbook.write(fileOut);
                    log.info("Cap nhat du lieu vao file Excel thanh cong.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                log.error("Loi trong qua trinh xu ly file. {}", derivativesFile);
            }
        }

        log.info("Ghi du lieu giao dich tu ngay {} den ngay {} vao file thanh cong", start, end);
    }


    @Override
    public void writeSpecificColumn(String tradingDate, String column) {
        String[] symbols = SymbolConstant.SYMBOLS;

        int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.DERIVATIVES_TRADING_DATE_COLUMN_INDEX));
        String dateToFind = DateHelper.parseDateFormat(tradingDate);
        int cellUpdated = excelHelper.findRowIndexByCellValue(statisticFile, symbols[0], tradingDateIdx, statisticInsertRow + 1, 10,  dateToFind);

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            List<String> sheetNames = excelHelper.getSheetNames(workbook);
            for (String symbol : sheetNames) {
                if(Arrays.asList(symbols).contains(symbol)){
                    log.info("Luu du lieu vao Sheet name {}", symbol);
                    String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);

                    int cidx = columnIdx.get(column);
                    if(cidx > 11 && cidx < 19){
                        OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndHashDate(symbol, hashDate);
                        log.info("Ghi du lieu {} ma {} cho ngay {}", column, symbol, tradingDate);
                        Sheet sheet = workbook.getSheet(symbol);
                        Row row = sheet.getRow(cellUpdated);

                        if(!Objects.isNull(orderBookEntity)){
                            switch (cidx){
                                case 12:
                                    double buyOrder = orderBookEntity.getBuyOrder() ;
                                    excelHelper.updateCellDouble(workbook, row, cidx, buyOrder, false);
                                    break;

                                case 13:
                                    double sellOrder = orderBookEntity.getSellOrder() ;
                                    excelHelper.updateCellDouble(workbook, row, cidx, sellOrder, false);
                                    break;

                                case 14:
                                    double bigBuyOrder = orderBookEntity.getBigBuyOrder() ;
                                    excelHelper.updateCellDouble(workbook, row, cidx, bigBuyOrder, false);
                                    break;

                                case 15:
                                    double bigSellOrder = orderBookEntity.getBigSellOrder() ;
                                    excelHelper.updateCellDouble(workbook, row, cidx, bigSellOrder, false);
                                    break;

                                default:
                                    log.info("Khong tim thay gia tri cot tuong ung cua {}", column);
                                    break;
                            }
                        }
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
            log.error("Loi trong qua trinh truy xuat file. {}", statisticFile);
        }

    }

    private static ArrayList<String> daysInRange(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;

        ArrayList<String> daysBetween = new ArrayList<>();
        endDate = endDate.plusDays(1);
        while (!currentDate.isEqual(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if(dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY){
                log.info("Bo qua ngay cuoi tuan {} ", currentDate);
                continue;
            }
            daysBetween.add(currentDate.toString());
            currentDate = currentDate.plusDays(1);
        }
        return daysBetween;
    }
}
