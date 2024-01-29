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
import java.time.LocalDate;
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
//                    "proprietary-buy-volume", 7,
//                    "proprietary-sell-volume", 8,
//                    "proprietary-total-volume", 9,
//                    "proprietary-total-value", 10,
//                    "intraday-buy-order", 12,
//                    "intraday-sell-order", 13,
//                    "intraday-big-buy-order", 14,
//                    "intraday-big-sell-order", 15,
//                    "intraday-buy-volume", 16
//                    "percentage-change", 17,
//                    "total-volume", 18,
                    "price-range", 22
            );


    private final ExcelHelper excelHelper;
    private final ProprietaryTradingRepository proprietaryTradingRepository;
    private final ForeignTradingRepository foreignTradingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DerivativesTradingRepository derivativesTradingRepository;
    private final OrderBookRepository orderBookRepository;
    private final IndexStatisticRepository indexStatisticRepository;
    private final Environment env;

    @Value("${statistics.file.path}")
    private String statisticFile;

    @Value("${derivatives.file.path}")
    private String derivativesFile;

    @Value("${statistics.insert.new.row.index}")
    private int statisticInsertRow;

    @Value("${statistics.moneyflow.insert.new.row.index}")
    private int moneyflowInsertRow;

    @Value("${derivatives.insert.row.index}")
    private int derivativesInsertRow;

    @Autowired
    public StatisticsServiceImpl(ExcelHelper excelHelper,
                                 ProprietaryTradingRepository proprietaryTradingRepository,
                                 StockPriceRepository stockPriceRepository,
                                 ForeignTradingRepository foreignTradingRepository,
                                 DerivativesTradingRepository derivativesTradingRepository,
                                 OrderBookRepository orderBookRepository,
                                 IndexStatisticRepository indexStatisticRepository,
                                 Environment env
                                 ){
        this.excelHelper = excelHelper;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
        this.foreignTradingRepository = foreignTradingRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.derivativesTradingRepository = derivativesTradingRepository;
        this.orderBookRepository = orderBookRepository;
        this.indexStatisticRepository = indexStatisticRepository;
        this.env = env;
    }


    @Override
    public void writeSpecificDate(String symbol, String tradingDate) {
        String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);
        ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
//        IntradayOrderEntity intradayOrderEntity = intradayOrderRepository.findIntradayOrderEntitiesBySymbolAndHashDate(symbol, hashDate);
        OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndHashDate(symbol, hashDate);
        ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesByHashDate(hashDate);
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

        ArrayList<String> tradingDates = DateHelper.daysInRange(startDate, endDate);

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
                log.info("Cap nhat du lieu giao dich trong ngay {}", tradingDate);

                ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesBySymbolAndHashDate(symbol, hashDate);

                // xu ly cho truong hop nghi le
                if (Objects.isNull(foreignTradingEntity)) {
                    log.info("Khong tim thay du lieu giao dich trong ngay {}", tradingDate);
                    continue;
                }

                OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndHashDate(symbol, hashDate);
//                IntradayOrderEntity intradayOrderEntity = intradayOrderRepository.findIntradayOrderEntitiesBySymbolAndHashDate(symbol, hashDate);
                ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesByHashDate(hashDate);
                StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesBySymbolAndHashDate(symbol, hashDate);

                log.info("Ghi du lieu cua ma {} cho ngay {}", symbol, tradingDate);

                ZipSecureFile.setMinInflateRatio(0);
                Sheet sheet = workbook.getSheet(symbol);
                excelHelper.insertNewRow(sheet, statisticInsertRow);
                Row row = sheet.getRow(statisticInsertRow);

                if (!Objects.isNull(proprietaryTradingEntity)) {
                    double proprietaryBuyVolume = proprietaryTradingEntity.getBuyVolume() == null ? 0 : proprietaryTradingEntity.getBuyVolume();
                    double proprietarySellVolume = proprietaryTradingEntity.getSellVolume() == null ? 0 : proprietaryTradingEntity.getSellVolume();
                    double proprietaryNetValue = proprietaryTradingEntity.getTotalNetValue() == null ? proprietaryTradingEntity.getBuyValue() - proprietaryTradingEntity.getSellValue() : proprietaryTradingEntity.getTotalNetValue();
                    excelHelper.updateCellDouble(workbook, row, proprietaryBuyVolIdx, proprietaryBuyVolume, false);
                    excelHelper.updateCellDouble(workbook, row, proprietarySellVolIdx, proprietarySellVolume, false);
                    excelHelper.updateCellDouble(workbook, row, proprietaryNetVolIdx, proprietaryBuyVolume - proprietarySellVolume, false);
                    excelHelper.updateCellDouble(workbook, row, proprietaryNetValIdx, proprietaryNetValue, false);
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
                String priceRange = stockPriceEntity.getPriceRange().replace("%", "");

                excelHelper.updateCellDate(workbook, row, tradingDateIdx, foreignTradingEntity.getTradingDate().toString());
                excelHelper.updateCellDouble(workbook, row, foreignBuyVolIdx, foreignTradingEntity.getBuyVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignSellVolIdx, foreignTradingEntity.getSellVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignNetVolIdx, foreignTradingEntity.getBuyVolume() - foreignTradingEntity.getSellVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignNetValIdx, foreignTradingEntity.getBuyValue() - foreignTradingEntity.getSellValue(), false);
                excelHelper.updateCellDouble(workbook, row, totalVolumeIdx, totalVolume, false);
                excelHelper.updateCellDouble(workbook, row, percenChangeIdx, Double.parseDouble(percenChange) / 100, true);
                excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PRICE_RANGE_COLUMN_INDEX), Double.parseDouble(priceRange)/100, true);

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
                    ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesByHashDate(hashDate);
                    StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesBySymbolAndHashDate(symbol, hashDate);

                    log.info("Ghi du lieu cua ma {} cho ngay {}", symbol, tradingDate);

                    Sheet sheet = workbook.getSheet(symbol);
                    excelHelper.insertNewRow(sheet, statisticInsertRow);
                    Row row = sheet.getRow(statisticInsertRow);

                    if(!Objects.isNull(proprietaryTradingEntity)){
                        double proprietaryBuyVolume = proprietaryTradingEntity.getBuyVolume() == null ? 0 : proprietaryTradingEntity.getBuyVolume();
                        double proprietarySellVolume = proprietaryTradingEntity.getSellVolume() == null ? 0 : proprietaryTradingEntity.getSellVolume();
                        double proprietaryNetValue = proprietaryTradingEntity.getTotalNetValue() == null ? proprietaryTradingEntity.getBuyValue() - proprietaryTradingEntity.getSellValue() : proprietaryTradingEntity.getTotalNetValue();
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX), proprietaryBuyVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX), proprietarySellVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX), proprietaryBuyVolume - proprietarySellVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX), proprietaryNetValue, false);
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

        ArrayList<String> tradingDates = DateHelper.daysInRange(startDate, endDate);

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
    public void writeSpecificDataAllSymbolSpecificDate(String tradingDate, String column) {
        String[] symbols = SymbolConstant.SYMBOLS;

        int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));
        String dateToFind = DateHelper.parseDateFormat(tradingDate);
        int cellUpdated = excelHelper.findRowIndexByCellValue(statisticFile, symbols[0], tradingDateIdx, statisticInsertRow + 1, 100,  dateToFind);

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

    @Override
    public void writeSpecificDataSpecificSymbolFromTo(String symbol, String start, String end, String column) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        ArrayList<String> tradingDates = DateHelper.daysInRange(startDate, endDate);

        int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));

        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            log.info("Opened filed");
            boolean fileIsChanged = false;
            for (String tradingDate : tradingDates) {
                int cidx = columnIdx.get(column);
                String dateToFind = DateHelper.parseDateFormat(tradingDate);
                int rowUpdated = excelHelper.findRowIndexByCellValue(statisticFile, symbol, tradingDateIdx, statisticInsertRow + 1, 100,  dateToFind);

                if(cidx > 17 && cidx < 23 && rowUpdated != -1){
                    String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
                    StockPriceEntity entity = stockPriceRepository.findStockPriceEntitiesByHashDate(hashDate);
                    if(!Objects.isNull(entity)){
                        switch (cidx){
                            case 22:
                                double high = entity.getHighestPrice();
                                double low = entity.getLowestPrice();
                                double volatility = ((high - low) / high) *100;
                                Sheet sheet = workbook.getSheet(symbol);
                                Row row = sheet.getRow(rowUpdated);
                                excelHelper.updateCellDouble(workbook, row, cidx, volatility/100, true);
                                fileIsChanged = true;
                                log.info("Gia tri cua cot {}:{} vao sheet {} cho ngay {} thanh cong", column, volatility, symbol, tradingDate);
                                break;

                            default:
                                log.info("Khong tim thay gia tri cot tuong ung cua {}", column);
                                break;
                        }
                    }
                }
            }

            if(fileIsChanged){
                // Save the workbook to a file
                try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                    workbook.write(fileOut);
                    log.info("Cap nhat du lieu cua column {} vao file Excel thanh cong.", column);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", statisticFile);
        }
    }

    @Override
    public void writeIndexAnalyzedDateToDate(String from, String to) {
        LocalDate startDate = LocalDate.parse(from);
        LocalDate endDate = LocalDate.parse(to);
        ArrayList<String> tradingDates = DateHelper.daysInRange(startDate, endDate);

        String sheetName = StockConstant.MONEY_FLOW;
        if(!tradingDates.isEmpty()){
            try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
                for (String tradingDate : tradingDates) {
                    String vn30HashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.VN30);
                    String bluechipHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.BLUE_CHIP);
                    String midcapHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.MID_CAP);
                    String smallcapHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.SMALL_CAP);
                    String bankHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.BANKS);
                    String stockHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.STOCKS);
                    String bdsHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.BDS);
                    String kcnHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.BDS_KCN);
                    String retailHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.RETAIL);
                    String logisticsHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.LOGISTICS);
                    String textileHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.TEXTILE);
                    String woodHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.WOOD);
                    String oilHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.OIL_GAS);
                    String seadfoodHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.SEAFOOD);
                    String materialsHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.METARIAL);
                    String steelHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.STEEL);
                    String constructionHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.CONSTRUCTION);
                    String eletricHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.ELECTRIC);
                    String chemistryHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.CHEMISTRY_FERTILIZER);

                    String vn30Percentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(vn30HashDate);
                    String bluechipPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bluechipHashDate);
                    String midcapPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(midcapHashDate);
                    String smallcapPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(smallcapHashDate);
                    String bankPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bankHashDate);
                    String stockPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(stockHashDate);
                    String bdsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bdsHashDate);
                    String kcnPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(kcnHashDate);
                    String retailPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(retailHashDate);
                    String logisticsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(logisticsHashDate);
                    String textilePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(textileHashDate);
                    String woodPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(woodHashDate);
                    String oilPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(oilHashDate);
                    String seafoodPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(seadfoodHashDate);
                    String materialsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(materialsHashDate);
                    String steelPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(steelHashDate);
                    String constructionPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(constructionHashDate);
                    String eletricPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(eletricHashDate);
                    String chemistryPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(chemistryHashDate);


                    // xu ly cho truong hop nghi le
                    if (vn30Percentage.isEmpty()) {
                        log.info("Khong tim thay du lieu giao dich trong ngay {}", tradingDate);
                        continue;
                    }

                    log.info("Ghi du lieu phan tich index cho ngay {}", tradingDate);
                    ZipSecureFile.setMinInflateRatio(0);

                    Sheet sheet = workbook.getSheet(sheetName);
                    excelHelper.insertNewRow(sheet, moneyflowInsertRow);
                    Row row = sheet.getRow(moneyflowInsertRow);

                    String vn30String = vn30Percentage.replace("%", "");
                    String bluechipString = bluechipPercentage.replace("%", "");
                    String midcapString = midcapPercentage.replace("%", "");
                    String smallcapString = smallcapPercentage.replace("%", "");
                    String bankString = bankPercentage.replace("%", "");
                    String stockString = stockPercentage.replace("%", "");
                    String bdsString = bdsPercentage.replace("%", "");
                    String kcnString = kcnPercentage.replace("%", "");
                    String retailString = retailPercentage.replace("%", "");
                    String logisticsString = logisticsPercentage.replace("%", "");
                    String woodString = woodPercentage.replace("%", "");
                    String oilString = oilPercentage.replace("%", "");
                    String seafoodString = seafoodPercentage.replace("%", "");
                    String steelString = steelPercentage.replace("%", "");
                    String materialString = materialsPercentage.replace("%", "");
                    String constructionString = constructionPercentage.replace("%", "");
                    String textileString = textilePercentage.replace("%", "");
                    String electricString = eletricPercentage.replace("%", "");
                    String chemistryString = chemistryPercentage.replace("%", "");

                    excelHelper.updateCellDate(workbook, row, 1, tradingDate);
                    excelHelper.updateCellDouble(workbook, row, 2, Double.parseDouble(vn30String) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 3, Double.parseDouble(bluechipString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 4, Double.parseDouble(midcapString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 5, Double.parseDouble(smallcapString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 6, Double.parseDouble(bankString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 7, Double.parseDouble(stockString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 8, Double.parseDouble(bdsString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 9, Double.parseDouble(kcnString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 10, Double.parseDouble(steelString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 11, Double.parseDouble(retailString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 12, Double.parseDouble(oilString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 13, Double.parseDouble(constructionString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 14, Double.parseDouble(logisticsString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 15, Double.parseDouble(textileString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 16, Double.parseDouble(seafoodString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 17, Double.parseDouble(woodString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 18, Double.parseDouble(materialString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 19, Double.parseDouble(electricString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 20, Double.parseDouble(chemistryString) / 100, true);

                }

                // Save the workbook to a file
                try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                    workbook.write(fileOut);
                    log.info("Cap nhat du lieu vao file Excel thanh cong.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                log.error("Loi trong qua trinh xu ly file. {}", derivativesFile);
            }
        }

        log.info("Ghi du lieu phan tich index tu ngay {} den ngay {} vao file thanh cong", from , to);
    }

}
