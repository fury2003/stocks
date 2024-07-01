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
    private final ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository;
    private final ForeignTradingStatisticRepository foreignTradingStatisticRepository;
    private final ForeignTradingRepository foreignTradingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DerivativesTradingRepository derivativesTradingRepository;
    private final OrderBookRepository orderBookRepository;
    private final IndexStatisticRepository indexStatisticRepository;
    private final Environment env;

    @Value("${data.trading.file.path}")
    private String dataFile;

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
                                 ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository, ForeignTradingStatisticRepository foreignTradingStatisticRepository, StockPriceRepository stockPriceRepository,
                                 ForeignTradingRepository foreignTradingRepository,
                                 DerivativesTradingRepository derivativesTradingRepository,
                                 OrderBookRepository orderBookRepository,
                                 IndexStatisticRepository indexStatisticRepository,
                                 Environment env
                                 ){
        this.excelHelper = excelHelper;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
        this.proprietaryTradingStatisticRepository = proprietaryTradingStatisticRepository;
        this.foreignTradingStatisticRepository = foreignTradingStatisticRepository;
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
            data.setProprietaryTotalNetValue(proprietaryTradingEntity.getTotalNetValue());
        }

        if(!Objects.isNull(orderBookEntity)){
            data.setBuyOrder(orderBookEntity.getBuyOrder());
            data.setSellOrder(orderBookEntity.getSellOrder());
            data.setMediumBuyOrder(orderBookEntity.getMediumBuyOrder());
            data.setMediumSellOrder(orderBookEntity.getMediumSellOrder());
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
        int orderMediumBuyIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_MEDIUM_BUY_ORDER_COLUMN_INDEX));
        int orderMediumSellIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_MEDIUM_SELL_ORDER_COLUMN_INDEX));
        int orderLargeBuyIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_LARGE_BUY_ORDER_COLUMN_INDEX));
        int orderLargeSellIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_LARGE_SELL_ORDER_COLUMN_INDEX));
        int orderBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX));
        int orderSellVolIdx = Integer.parseInt(env.getProperty(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX));

        int totalVolumeIdx = Integer.parseInt(env.getProperty(StockConstant.TOTAL_VOL_COLUMN_INDEX));
        int percenChangeIdx = Integer.parseInt(env.getProperty(StockConstant.PERCENTAGE_CHANGE_COLUMN_INDEX));

        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (String tradingDate : tradingDates) {
                String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
                log.info("Cap nhat du lieu giao dich trong ngay {}", tradingDate);

                ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesByHashDate(hashDate);

                // xu ly cho truong hop nghi le
                if (Objects.isNull(foreignTradingEntity)) {
                    log.info("Khong tim thay du lieu giao dich trong ngay {}", tradingDate);
                    continue;
                }

                OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesByHashDate(hashDate);
//                IntradayOrderEntity intradayOrderEntity = intradayOrderRepository.findIntradayOrderEntitiesBySymbolAndHashDate(symbol, hashDate);
                ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesByHashDate(hashDate);
                StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesByHashDate(hashDate);

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
                    double mediumBuyOrder = orderBookEntity.getMediumBuyOrder();
                    double mediumSellOrder = orderBookEntity.getMediumSellOrder();
                    double largeBuyOrder = orderBookEntity.getLargeBuyOrder();
                    double largeSellOrder = orderBookEntity.getLargeSellOrder();
                    double buyOrderVol = orderBookEntity.getBuyVolume();
                    double sellOrderVol = orderBookEntity.getSellVolume();
                    excelHelper.updateCellDouble(workbook, row, orderBuyIdx, buyOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderSellIdx, sellOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderMediumBuyIdx, mediumBuyOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderMediumSellIdx, mediumSellOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderLargeBuyIdx, largeBuyOrder, false);
                    excelHelper.updateCellDouble(workbook, row, orderLargeSellIdx, largeSellOrder, false);
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
            try (FileOutputStream fileOut = new FileOutputStream(dataFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", dataFile);
        }
        log.info("Ghi du lieu giao dich tu ngay {} den ngay {} vao file thanh cong", start, end);
    }

    @Override
    public void writeAllForSpecificDate(String tradingDate) {
        String[] symbols = SymbolConstant.SYMBOLS;

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));

            List<String> sheetNames = excelHelper.getSheetNames(workbook);
            for (String symbol : sheetNames) {
                if(Arrays.asList(symbols).contains(symbol)){
                    log.info("Luu du lieu vao Sheet name {}", symbol);
                    String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);
                    ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesByHashDate(hashDate);
                    OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesByHashDate(hashDate);
                    ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findByTradingDateAndSymbol(LocalDate.parse(tradingDate), symbol);
                    StockPriceEntity stockPriceEntity = stockPriceRepository.findStockPriceEntitiesByHashDate(hashDate);

                    if(stockPriceEntity == null){
                        log.info("Ko tim thay du lieu giao dich {} trong ngay {}", symbol, tradingDate);
                        continue;
                    }

                    log.info("Ghi du lieu cua ma {} cho ngay {}", symbol, tradingDate);

                    Sheet sheet = workbook.getSheet(symbol);
                    excelHelper.insertNewRow(sheet, statisticInsertRow);
                    Row row = sheet.getRow(statisticInsertRow);

                    double totalVolume = stockPriceEntity.getTotalVolume();
                    String percenChange = stockPriceEntity.getPercentageChange().replace("%", "");
                    String priceRange = stockPriceEntity.getPriceRange().replace("%", "");
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.TOTAL_VOL_COLUMN_INDEX), totalVolume, false);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PERCENTAGE_CHANGE_COLUMN_INDEX), Double.parseDouble(percenChange)/100, true);
                    excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PRICE_RANGE_COLUMN_INDEX), Double.parseDouble(priceRange)/100, true);
                    
                    if(!Objects.isNull(proprietaryTradingEntity)){
                        double proprietaryBuyVolume = proprietaryTradingEntity.getBuyVolume() == null ? 0 : proprietaryTradingEntity.getBuyVolume();
                        double proprietarySellVolume = proprietaryTradingEntity.getSellVolume() == null ? 0 : proprietaryTradingEntity.getSellVolume();
                        double proprietaryNetValue = proprietaryTradingEntity.getTotalNetValue() == null ? proprietaryTradingEntity.getBuyValue() - proprietaryTradingEntity.getSellValue() : proprietaryTradingEntity.getTotalNetValue();
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX), proprietaryBuyVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX), proprietarySellVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX), proprietaryBuyVolume - proprietarySellVolume, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX), proprietaryNetValue, false);
                    }else{
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX), 0.0, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX), 0.0, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX), 0.0, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX), 0.0, false);
                    }

                    if(!Objects.isNull(orderBookEntity)){
                        double buyOrder = orderBookEntity.getBuyOrder();
                        double sellOrder = orderBookEntity.getSellOrder();
                        double mediumBuyOrder = orderBookEntity.getMediumBuyOrder();
                        double mediumSellOrder = orderBookEntity.getMediumSellOrder();
                        double largeBuyOrder = orderBookEntity.getLargeBuyOrder();
                        double largeSellOrder = orderBookEntity.getLargeSellOrder();
                        double buyOrderVol = orderBookEntity.getBuyVolume();
                        double sellOrderVol = orderBookEntity.getSellVolume();
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX), buyOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX), sellOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_BUY_ORDER_COLUMN_INDEX), mediumBuyOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_SELL_ORDER_COLUMN_INDEX), mediumSellOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_LARGE_BUY_ORDER_COLUMN_INDEX), largeBuyOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_LARGE_SELL_ORDER_COLUMN_INDEX), largeSellOrder, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX), buyOrderVol, false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX), sellOrderVol, false);
                    }

                    if(!Objects.isNull(foreignTradingEntity)){
                        excelHelper.updateCellDate(workbook, row, tradingDateIdx, foreignTradingEntity.getTradingDate().toString());
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_BUY_VOL_COLUMN_INDEX), foreignTradingEntity.getBuyVolume(), false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_SELL_VOL_COLUMN_INDEX), foreignTradingEntity.getSellVolume(), false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_TOTAL_NET_VOL_COLUMN_INDEX), foreignTradingEntity.getBuyVolume() - foreignTradingEntity.getSellVolume(), false);
                        excelHelper.updateCellDouble(workbook, row, getExcelColumnIndex(StockConstant.FOREIGN_TOTAL_NET_VAL_COLUMN_INDEX), foreignTradingEntity.getBuyValue() - foreignTradingEntity.getSellValue(), false);
                    }
                }
            }

            // Save the workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(dataFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", dataFile);
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
        int cellUpdated = excelHelper.findRowIndexByCellValue(dataFile, symbols[0], tradingDateIdx, statisticInsertRow + 1, 100,  dateToFind);

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
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
                                    double mediumBuyOrder = orderBookEntity.getMediumBuyOrder() ;
                                    excelHelper.updateCellDouble(workbook, row, cidx, mediumBuyOrder, false);
                                    break;

                                case 15:
                                    double mediumSellOrder = orderBookEntity.getMediumSellOrder() ;
                                    excelHelper.updateCellDouble(workbook, row, cidx, mediumSellOrder, false);
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
            try (FileOutputStream fileOut = new FileOutputStream(dataFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", dataFile);
        }

    }

    @Override
    public void writeSpecificDataSpecificSymbolFromTo(String symbol, String start, String end, String column) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        ArrayList<String> tradingDates = DateHelper.daysInRange(startDate, endDate);

        int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));

        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            log.info("Opened filed");
            boolean fileIsChanged = false;
            for (String tradingDate : tradingDates) {
                int cidx = columnIdx.get(column);
                String dateToFind = DateHelper.parseDateFormat(tradingDate);
                int rowUpdated = excelHelper.findRowIndexByCellValue(dataFile, symbol, tradingDateIdx, statisticInsertRow + 1, 100,  dateToFind);

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
                try (FileOutputStream fileOut = new FileOutputStream(dataFile)) {
                    workbook.write(fileOut);
                    log.info("Cap nhat du lieu cua column {} vao file Excel thanh cong.", column);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh truy xuat file. {}", dataFile);
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
                    String animalsHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.ANIMALS);
                    String insuranceHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.INSURANCE);
                    String airlineHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.AIRLINE);
                    String plasticHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.PLASTIC);
                    String techHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.TECH);

                    String vn30Percentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(vn30HashDate);
                    String bluechipPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bluechipHashDate);
                    String midcapPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(midcapHashDate);
                    String smallcapPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(smallcapHashDate);

                    // bank
                    String bankPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bankHashDate);
                    Integer bankBuyVol = orderBookRepository.getBankBuyVolume(startDate);
                    Integer bankSellVol = orderBookRepository.getBankSellVolume(startDate);

                    // stock
                    String stockPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(stockHashDate);
                    Integer stockBuyVol = orderBookRepository.getStockBuyVolume(startDate);
                    Integer stockSellVol = orderBookRepository.getStockSellVolume(startDate);

                    // real estate
                    String bdsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bdsHashDate);
                    Integer bdsBuyVol = orderBookRepository.getRealEstateBuyVolume(startDate);
                    Integer bdsSellVol = orderBookRepository.getRealEstateSellVolume(startDate);

                    // kcn
                    String kcnPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(kcnHashDate);
                    Integer kcnBuyVol = orderBookRepository.getKCNBuyVolume(startDate);
                    Integer kcnSellVol = orderBookRepository.getKCNSellVolume(startDate);

                    // steels
                    String steelPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(steelHashDate);
                    Integer steelBuyVol = orderBookRepository.getSteelBuyVolume(startDate);
                    Integer steelSellVol = orderBookRepository.getSteelSellVolume(startDate);

                    // retails
                    String retailPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(retailHashDate);
                    Integer retailBuyVol = orderBookRepository.getRetailsBuyVolume(startDate);
                    Integer retailSellVol = orderBookRepository.getRetailsSellVolume(startDate);

                    // logistics
                    String logisticsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(logisticsHashDate);
                    Integer logisticBuyVol = orderBookRepository.getLogisticBuyVolume(startDate);
                    Integer logisticSellVol = orderBookRepository.getLogisticSellVolume(startDate);

                    // textile
                    String textilePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(textileHashDate);
                    Integer textileBuyVol = orderBookRepository.getTextileBuyVolume(startDate);
                    Integer textileSellVol = orderBookRepository.getTextileSellVolume(startDate);

                    // wood
                    String woodPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(woodHashDate);
                    Integer woodBuyVol = orderBookRepository.getWoodBuyVolume(startDate);
                    Integer woodSellVol = orderBookRepository.getWoodSellVolume(startDate);

                    // oil
                    String oilPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(oilHashDate);
                    Integer oilBuyVol = orderBookRepository.getOilBuyVolume(startDate);
                    Integer oilSellVol = orderBookRepository.getOilSellVolume(startDate);

                    // seafood
                    String seafoodPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(seadfoodHashDate);
                    Integer seafoodBuyVol = orderBookRepository.getSeafoodBuyVolume(startDate);
                    Integer seafoodSellVol = orderBookRepository.getSeafoodSellVolume(startDate);

                    // materials
                    String materialsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(materialsHashDate);
                    Integer materialBuyVol = orderBookRepository.getMaterialBuyVolume(startDate);
                    Integer materialSellVol = orderBookRepository.getMaterialSellVolume(startDate);

                    // construction
                    String constructionPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(constructionHashDate);
                    Integer constructionBuyVol = orderBookRepository.getConstructionBuyVolume(startDate);
                    Integer constructionSellVol = orderBookRepository.getConstructionSellVolume(startDate);

                    // electric
                    String electricPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(eletricHashDate);
                    Integer electricBuyVol = orderBookRepository.getElectricBuyVolume(startDate);
                    Integer electricSellVol = orderBookRepository.getElectricSellVolume(startDate);

                    // chemistry
                    String chemistryPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(chemistryHashDate);
                    Integer chemistryBuyVol = orderBookRepository.getChemistryBuyVolume(startDate);
                    Integer chemistrySellVol = orderBookRepository.getChemistrySellVolume(startDate);

                    // animals
                    String animalsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(animalsHashDate);
                    Integer animalsBuyVol = orderBookRepository.getAnimalsBuyVolume(startDate);
                    Integer animalsSellVol = orderBookRepository.getAnimalsSellVolume(startDate);

                    // insurance
                    String insurancePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(insuranceHashDate);
                    Integer insuranceBuyVol = orderBookRepository.getInsuranceBuyVolume(startDate);
                    Integer insuranceSellVol = orderBookRepository.getInsuranceSellVolume(startDate);

                    // airline
                    String airlinePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(airlineHashDate);
                    Integer airlineBuyVol = orderBookRepository.getAirlineBuyVolume(startDate);
                    Integer airlineSellVol = orderBookRepository.getAirlineSellVolume(startDate);

                    // plastic
                    String plasticPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(plasticHashDate);
                    Integer plasticBuyVol = orderBookRepository.getPlasticBuyVolume(startDate);
                    Integer plasticSellVol = orderBookRepository.getPlasticSellVolume(startDate);

                    // tech
                    String techPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(techHashDate);
                    Integer techBuyVol = orderBookRepository.getTechBuyVolume(startDate);
                    Integer techSellVol = orderBookRepository.getTechSellVolume(startDate);

                    // xu ly cho truong hop nghi le
                    if (vn30Percentage.isEmpty()) {
                        log.info("Khong tim thay du lieu giao dich trong ngay {}", tradingDate);
                        continue;
                    }

                    log.info("Ghi du lieu phan tich index cho ngay {}", tradingDate);
                    ZipSecureFile.setMinInflateRatio(0);

                    Sheet sheet = workbook.getSheet(sheetName);
                    excelHelper.insertNewRow(sheet, 2);
                    Row row = sheet.getRow(2);

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
                    String electricString = electricPercentage.replace("%", "");
                    String chemistryString = chemistryPercentage.replace("%", "");
                    String animalsString = animalsPercentage.replace("%", "");
                    String insuranceString = insurancePercentage.replace("%", "");
                    String airlineString = airlinePercentage.replace("%", "");
                    String plasticString = plasticPercentage.replace("%", "");
                    String techString = techPercentage.replace("%", "");

                    excelHelper.updateCellDate(workbook, row, 1, tradingDate);
                    excelHelper.updateCellDouble(workbook, row, 2, Double.parseDouble(vn30String) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 3, Double.parseDouble(bluechipString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 4, Double.parseDouble(midcapString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 5, Double.parseDouble(smallcapString) / 100, true);

                    excelHelper.updateCellDouble(workbook, row, 6,Double.parseDouble(bankString) / 100,true);
                    excelHelper.updateCellInt(workbook, row, 7, bankBuyVol);
                    excelHelper.updateCellInt(workbook, row, 8, bankSellVol);

                    excelHelper.updateCellDouble(workbook, row, 9, Double.parseDouble(stockString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 10, stockBuyVol);
                    excelHelper.updateCellInt(workbook, row, 11, stockSellVol);

                    excelHelper.updateCellDouble(workbook, row, 12, Double.parseDouble(bdsString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 13, bdsBuyVol);
                    excelHelper.updateCellInt(workbook, row, 14, bdsSellVol);

                    excelHelper.updateCellDouble(workbook, row, 15, Double.parseDouble(kcnString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 16, kcnBuyVol);
                    excelHelper.updateCellInt(workbook, row, 17, kcnSellVol);

                    excelHelper.updateCellDouble(workbook, row, 18, Double.parseDouble(steelString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 19, steelBuyVol);
                    excelHelper.updateCellInt(workbook, row, 20, steelSellVol);

                    excelHelper.updateCellDouble(workbook, row, 21, Double.parseDouble(retailString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 22, retailBuyVol);
                    excelHelper.updateCellInt(workbook, row, 23, retailSellVol);

                    excelHelper.updateCellDouble(workbook, row, 24, Double.parseDouble(oilString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 25, oilBuyVol);
                    excelHelper.updateCellInt(workbook, row, 26, oilSellVol);

                    excelHelper.updateCellDouble(workbook, row, 27, Double.parseDouble(constructionString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 28, constructionBuyVol);
                    excelHelper.updateCellInt(workbook, row, 29, constructionSellVol);

                    excelHelper.updateCellDouble(workbook, row, 30, Double.parseDouble(logisticsString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 31, logisticBuyVol);
                    excelHelper.updateCellInt(workbook, row, 32, logisticSellVol);

                    excelHelper.updateCellDouble(workbook, row, 33, Double.parseDouble(textileString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 34, textileBuyVol);
                    excelHelper.updateCellInt(workbook, row, 35, textileSellVol);

                    excelHelper.updateCellDouble(workbook, row, 36, Double.parseDouble(seafoodString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 37, seafoodBuyVol);
                    excelHelper.updateCellInt(workbook, row, 38, seafoodSellVol);

                    excelHelper.updateCellDouble(workbook, row, 39, Double.parseDouble(woodString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 40, woodBuyVol);
                    excelHelper.updateCellInt(workbook, row, 41, woodSellVol);

                    excelHelper.updateCellDouble(workbook, row, 42, Double.parseDouble(materialString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 43, materialBuyVol);
                    excelHelper.updateCellInt(workbook, row, 44, materialSellVol);

                    excelHelper.updateCellDouble(workbook, row, 45, Double.parseDouble(electricString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 46, electricBuyVol);
                    excelHelper.updateCellInt(workbook, row, 47, electricSellVol);

                    excelHelper.updateCellDouble(workbook, row, 48, Double.parseDouble(chemistryString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 49, chemistryBuyVol);
                    excelHelper.updateCellInt(workbook, row, 50, chemistrySellVol);

                    excelHelper.updateCellDouble(workbook, row, 51, Double.parseDouble(animalsString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 52, animalsBuyVol);
                    excelHelper.updateCellInt(workbook, row, 53, animalsSellVol);

                    excelHelper.updateCellDouble(workbook, row, 54, Double.parseDouble(insuranceString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 55, insuranceBuyVol);
                    excelHelper.updateCellInt(workbook, row, 56, insuranceSellVol);

                    excelHelper.updateCellDouble(workbook, row, 57, Double.parseDouble(airlineString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 58, airlineBuyVol);
                    excelHelper.updateCellInt(workbook, row, 59, airlineSellVol);

                    excelHelper.updateCellDouble(workbook, row, 60, Double.parseDouble(plasticString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 61, plasticBuyVol);
                    excelHelper.updateCellInt(workbook, row, 62, plasticSellVol);

                    excelHelper.updateCellDouble(workbook, row, 63, Double.parseDouble(techString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 64, techBuyVol);
                    excelHelper.updateCellInt(workbook, row, 65, techSellVol);
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

        log.info("Ghi du lieu phan tich index tu ngay {} den ngay {} vao file thanh cong", from , to);
    }

    @Override
    public void writePriceChangeMonthly(String sheetName, String from, String to) {
        LocalDate startDate = LocalDate.parse(from);
        LocalDate endDate = LocalDate.parse(to);

        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            String[] symbols = SymbolConstant.SYMBOLS;
            Map<String, Double> stockList = new HashMap<>();
            for (int i = 0; i < symbols.length; i++) {
                Double percentChange = stockPriceRepository.getMonthlyPercentageChange(symbols[i], startDate, endDate);
                stockList.put(symbols[i], percentChange);
            }

            ZipSecureFile.setMinInflateRatio(0);
            Sheet sheet = workbook.getSheet(sheetName);


            stockList.forEach((name, percentage) -> {
                if(percentage != null){
                    log.info("Name: " + name + ", Percentage: " + percentage);
                    excelHelper.insertNewRow(sheet, 1);
                    Row row = sheet.getRow(1);
                    excelHelper.updateCellString(workbook, row, 1, name);
                    excelHelper.updateCellDouble(workbook, row, 2, Double.valueOf(percentage)/100, true);
                }
            });

            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("Cap nhat du lieu vao file Excel thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }

    }

    @Override
    public void highlightOrderBook(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<String> symbolsBuy = orderBookRepository.getStrongBuy(date);
        List<String> symbolsSell = orderBookRepository.getStrongSell(date);

        Integer mediumBuy = getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_BUY_ORDER_COLUMN_INDEX);
        Integer mediumSell = getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_SELL_ORDER_COLUMN_INDEX);
        Integer largeBuy = getExcelColumnIndex(StockConstant.INTRADAY_LARGE_BUY_ORDER_COLUMN_INDEX);
        Integer largeSell = getExcelColumnIndex(StockConstant.INTRADAY_LARGE_SELL_ORDER_COLUMN_INDEX);

        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            ZipSecureFile.setMinInflateRatio(0);
            for (String symbol : symbolsBuy) {
                Sheet sheet = workbook.getSheet(symbol);
                if(sheet == null) {
                    log.info("Sheet {} not exist", symbol);
                    continue;
                }
                log.info("Highlight {}", symbol);
                Row updateRow = sheet.getRow(2);
                excelHelper.highlightOrderBook(workbook, updateRow, mediumBuy, true);
                excelHelper.highlightOrderBook(workbook, updateRow, largeBuy, true);
            }

            for (String symbol : symbolsSell) {
                Sheet sheet = workbook.getSheet(symbol);
                if(sheet == null) {
                    log.info("Sheet {} not exist", symbol);
                    continue;
                }
                log.info("Highlight {}", symbol);

                Row updateRow = sheet.getRow(2);
                excelHelper.highlightOrderBook(workbook, updateRow, mediumSell, false);
                excelHelper.highlightOrderBook(workbook, updateRow, largeSell, false);
            }

            try (FileOutputStream fileOut = new FileOutputStream(dataFile)) {
                workbook.write(fileOut);
                log.info("highligh thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", dataFile);
        }
    }

    @Override
    public void writeTopBuySell(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);

        // Foreign buy/sell
        List<ForeignTradingEntity> topForeignBuy = foreignTradingRepository.getTop10ForeignBuy(date);
        for (ForeignTradingEntity entity : topForeignBuy){
            ForeignTradingStatisticEntity statistic = foreignTradingStatisticRepository.findBySymbol(entity.getSymbol());
            if(entity.getTotalNetValue() >= statistic.getHighestBuyValue())
                    entity.setBiggestATH(true);
            else if(entity.getTotalNetValue() >= statistic.getSixMonthsHighestBuyValue())
                entity.setBiggest6M(true);
        }

        List<ForeignTradingEntity> topForeignSell = foreignTradingRepository.getTop10ForeignSell(date);
        for (ForeignTradingEntity entity : topForeignSell){
            ForeignTradingStatisticEntity statistic = foreignTradingStatisticRepository.findBySymbol(entity.getSymbol());
            if(entity.getTotalNetValue() <= statistic.getHighestSellValue())
                    entity.setSmallestATH(true);
            else if(entity.getTotalNetValue() <= statistic.getSixMonthsHighestSellValue())
                entity.setSmallest6M(true);

        }

        // Proprietary buy/sell
        List<ProprietaryTradingEntity> topProprietaryBuy = proprietaryTradingRepository.getTop10ProprietaryBuy(date);
        for (ProprietaryTradingEntity entity : topProprietaryBuy){
            ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(entity.getSymbol());
            if(entity.getTotalNetValue() >= statistic.getHighestBuyValue())
                entity.setBiggestATH(true);
            else if(entity.getTotalNetValue() >= statistic.getSixMonthsHighestBuyValue())
                entity.setBiggest6M(true);

        }

        List<ProprietaryTradingEntity> topProprietarySell = proprietaryTradingRepository.getTop10ProprietarySell(date);
        for (ProprietaryTradingEntity entity : topProprietarySell){
            ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(entity.getSymbol());
            if(entity.getTotalNetValue() <= statistic.getHighestSellValue())
                entity.setSmallestATH(true);
            else if(entity.getTotalNetValue() <= statistic.getSixMonthsHighestSellValue()){
                entity.setSmallest6M(true);
            }
        }

        excelHelper.writeTopBuySellToFile(StockConstant.TOP_FOREIGN_INTRADAY_SHEET, topForeignBuy, topForeignSell,true, tradingDate);
        excelHelper.writeTopBuySellToFile(StockConstant.TOP_FOREIGN_INTRADAY_SHEET, topForeignBuy, topForeignSell, false, tradingDate);
        log.info("Hoan thanh thong ke mua ban cua khoi ngoai vao sheet: {}", StockConstant.TOP_FOREIGN_INTRADAY_SHEET);


        excelHelper.writeTopBuySellToFile(StockConstant.TOP_PROPRIETARY_INTRADAY_SHEET, topProprietaryBuy, topProprietarySell,true, tradingDate);
        excelHelper.writeTopBuySellToFile(StockConstant.TOP_PROPRIETARY_INTRADAY_SHEET, topProprietaryBuy, topProprietarySell, false, tradingDate);
        log.info("Hoan thanh thong ke mua ban cua khoi ngoai vao sheet: {}", StockConstant.TOP_PROPRIETARY_INTRADAY_SHEET);

        excelHelper.highlightTopBuySell(StockConstant.TOP_FOREIGN_INTRADAY_SHEET, topForeignBuy, topForeignSell);
        excelHelper.highlightTopBuySell(StockConstant.TOP_PROPRIETARY_INTRADAY_SHEET, topProprietaryBuy, topProprietarySell);
        log.info("Hoan thanh format mua ban ki luc vao sheet: {}", StockConstant.TOP_PROPRIETARY_INTRADAY_SHEET);

    }

}
