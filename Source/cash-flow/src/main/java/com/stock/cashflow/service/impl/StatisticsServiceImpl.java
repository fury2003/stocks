package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.IndustryConstant;
import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.StockGroupsConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.TradingStatistics;
import com.stock.cashflow.exception.BadRequestException;
import com.stock.cashflow.persistence.dto.SymbolTotalNetValueDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private final ExcelHelper excelHelper;
    private final ProprietaryTradingRepository proprietaryTradingRepository;
    private final ForeignTradingRepository foreignTradingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final OrderBookRepository orderBookRepository;
    private final IndexStatisticRepository indexStatisticRepository;
    private final TradingDateRepository tradingDateRepository;
    private final ForeignTradingStatisticRepository foreignTradingStatisticRepository;
    private final ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository;

    private final Environment env;

    @Value("${data.trading.file.path}")
    private String dataFile;

    @Value("${orderbook.file.path}")
    private String orderbookFile;

    @Value("${statistics.file.path}")
    private String statisticFile;

    @Value("${statistics.insert.new.row.index}")
    private int statisticInsertRow;

    @Autowired
    public StatisticsServiceImpl(ExcelHelper excelHelper,
                                 ProprietaryTradingRepository proprietaryTradingRepository,
                                 StockPriceRepository stockPriceRepository,
                                 ForeignTradingRepository foreignTradingRepository,
                                 OrderBookRepository orderBookRepository,
                                 IndexStatisticRepository indexStatisticRepository,
                                 TradingDateRepository tradingDateRepository, ForeignTradingStatisticRepository foreignTradingStatisticRepository, ProprietaryTradingStatisticRepository proprietaryTradingStatisticRepository, Environment env
                                 ){
        this.excelHelper = excelHelper;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
        this.foreignTradingRepository = foreignTradingRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.orderBookRepository = orderBookRepository;
        this.indexStatisticRepository = indexStatisticRepository;
        this.tradingDateRepository = tradingDateRepository;
        this.foreignTradingStatisticRepository = foreignTradingStatisticRepository;
        this.proprietaryTradingStatisticRepository = proprietaryTradingStatisticRepository;
        this.env = env;
    }


    @Override
    public void writeSpecificDate(String symbol, String tradingDate) {
        String hashDate = DigestUtils.sha256Hex(tradingDate +  symbol);
        ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesBySymbolAndHashDate(symbol, hashDate);
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

        Long startId = tradingDateRepository.getIdByTradingDate(startDate);
        Long endId = tradingDateRepository.getIdByTradingDate(endDate);

        int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));

        int foreignBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_BUY_VOL_COLUMN_INDEX));
        int foreignSellVolIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_SELL_VOL_COLUMN_INDEX));
        int foreignNetVolIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_TOTAL_NET_VOL_COLUMN_INDEX));
        int foreignNetValIdx = Integer.parseInt(env.getProperty(StockConstant.FOREIGN_TOTAL_NET_VAL_COLUMN_INDEX));

        int proprietaryBuyVolIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_BUY_VOL_COLUMN_INDEX));
        int proprietarySellVolIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_SELL_VOL_COLUMN_INDEX));
        int proprietaryNetVolIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_TOTAL_NET_VOL_COLUMN_INDEX));
        int proprietaryNetValIdx = Integer.parseInt(env.getProperty(StockConstant.PROPRIETARY_TOTAL_NET_VALUE_COLUMN_INDEX));

        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            for (long i = startId; i < endId; i++) {
                LocalDate date = tradingDateRepository.getTradingDateById(i);
                String hashDate = DigestUtils.sha256Hex(date + symbol);
                log.info("Cap nhat du lieu giao dich trong ngay {}", date);

                ForeignTradingEntity foreignTradingEntity = foreignTradingRepository.findForeignTradingEntitiesByHashDate(hashDate);

                // xu ly cho truong hop nghi le
                if (Objects.isNull(foreignTradingEntity)) {
                    log.info("Khong tim thay du lieu giao dich trong ngay {}", date);
                    continue;
                }

                ProprietaryTradingEntity proprietaryTradingEntity = proprietaryTradingRepository.findProprietaryTradingEntitiesByHashDate(hashDate);

                log.info("Ghi du lieu cua ma {} cho ngay {}", symbol, date);

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

                excelHelper.updateCellDate(workbook, row, tradingDateIdx, foreignTradingEntity.getTradingDate().toString());
                excelHelper.updateCellDouble(workbook, row, foreignBuyVolIdx, foreignTradingEntity.getBuyVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignSellVolIdx, foreignTradingEntity.getSellVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignNetVolIdx, foreignTradingEntity.getBuyVolume() - foreignTradingEntity.getSellVolume(), false);
                excelHelper.updateCellDouble(workbook, row, foreignNetValIdx, foreignTradingEntity.getBuyValue() - foreignTradingEntity.getSellValue(), false);

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
    public void writeSpecificDataAllSymbolSpecificDate(String tradingDate, String column) {
        String[] symbols = SymbolConstant.SYMBOLS;

        String dateToFind = DateHelper.parseDateFormat(tradingDate);
        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(dataFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            List<String> sheetNames = excelHelper.getSheetNames(workbook);

            int cidx = 12;
            int ridx = 0;
            Sheet ssiSheet = workbook.getSheet("SSI");

            // find row to update
            for (int rowIndex = 2; rowIndex < 140; rowIndex++) {
                Row row = ssiSheet.getRow(rowIndex);
                if (row != null) {
                    // get data in trading date column
                    Cell cell = row.getCell(0);
                    if (cell != null) {
                        try {
                            if (dateToFind.equals(cell.getStringCellValue())){
                                ridx = rowIndex;
                                log.info("Tim thay ngay trung khop tai dong {}", rowIndex);
                                break;
                            }
                        } catch (IllegalStateException ex) {
                            log.error("Format date khong dung o cot date trong file Excel. {}", cell.getStringCellValue());
                            throw new BadRequestException("Format date khong dung o cot date trong file Excel.");
                        }
                    }
                }
            }

            for (String symbol : sheetNames) {
                if(Arrays.asList(symbols).contains(symbol)){
                    log.info("Luu du lieu vao Sheet name {}", symbol);
                    OrderBookEntity orderBookEntity = orderBookRepository.findOrderBookEntitiesBySymbolAndTradingDate(symbol, LocalDate.parse(tradingDate));
                    log.info("Ghi du lieu {} ma {} cho ngay {}", column, symbol, tradingDate);
                    Sheet sheet = workbook.getSheet(symbol);
                    Row row = sheet.getRow(ridx);

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
                    String pharmaHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.PHARMA);
                    String sugarHashDate = DigestUtils.sha256Hex(tradingDate + StockConstant.SUGAR);

                    String vn30Percentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(vn30HashDate);
                    String bluechipPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bluechipHashDate);
                    String midcapPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(midcapHashDate);
                    String smallcapPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(smallcapHashDate);

                    // bank
                    String bankPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bankHashDate);
                    Integer bankBuyVol = orderBookRepository.getBankBuyVolume(startDate);
                    Integer bankSellVol = orderBookRepository.getBankSellVolume(startDate);
                    Long bankTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.BANKS), startDate);

                    // stock
                    String stockPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(stockHashDate);
                    Integer stockBuyVol = orderBookRepository.getStockBuyVolume(startDate);
                    Integer stockSellVol = orderBookRepository.getStockSellVolume(startDate);
                    Long stockTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.STOCKS), startDate);

                    // real estate
                    String bdsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(bdsHashDate);
                    Integer bdsBuyVol = orderBookRepository.getRealEstateBuyVolume(startDate);
                    Integer bdsSellVol = orderBookRepository.getRealEstateSellVolume(startDate);
                    Long bdsTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.BDS), startDate);

                    // kcn
                    String kcnPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(kcnHashDate);
                    Integer kcnBuyVol = orderBookRepository.getKCNBuyVolume(startDate);
                    Integer kcnSellVol = orderBookRepository.getKCNSellVolume(startDate);
                    Long kcnTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.BDS_KCN), startDate);

                    // steels
                    String steelPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(steelHashDate);
                    Integer steelBuyVol = orderBookRepository.getSteelBuyVolume(startDate);
                    Integer steelSellVol = orderBookRepository.getSteelSellVolume(startDate);
                    Long steelTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.STEELS), startDate);

                    // retails
                    String retailPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(retailHashDate);
                    Integer retailBuyVol = orderBookRepository.getRetailsBuyVolume(startDate);
                    Integer retailSellVol = orderBookRepository.getRetailsSellVolume(startDate);
                    Long retailTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.RETAIL), startDate);

                    // logistics
                    String logisticsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(logisticsHashDate);
                    Integer logisticBuyVol = orderBookRepository.getLogisticBuyVolume(startDate);
                    Integer logisticSellVol = orderBookRepository.getLogisticSellVolume(startDate);
                    Long logisticTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.LOGISTICS), startDate);

                    // textile
                    String textilePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(textileHashDate);
                    Integer textileBuyVol = orderBookRepository.getTextileBuyVolume(startDate);
                    Integer textileSellVol = orderBookRepository.getTextileSellVolume(startDate);
                    Long textileTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.TEXTILE), startDate);

                    // wood
                    String woodPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(woodHashDate);
                    Integer woodBuyVol = orderBookRepository.getWoodBuyVolume(startDate);
                    Integer woodSellVol = orderBookRepository.getWoodSellVolume(startDate);
                    Long woodTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.WOOD), startDate);

                    // oil
                    String oilPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(oilHashDate);
                    Integer oilBuyVol = orderBookRepository.getOilBuyVolume(startDate);
                    Integer oilSellVol = orderBookRepository.getOilSellVolume(startDate);
                    Long oilTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.OIL_GAS), startDate);

                    // seafood
                    String seafoodPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(seadfoodHashDate);
                    Integer seafoodBuyVol = orderBookRepository.getSeafoodBuyVolume(startDate);
                    Integer seafoodSellVol = orderBookRepository.getSeafoodSellVolume(startDate);
                    Long seafoodTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.SEAFOOD), startDate);

                    // materials
                    String materialsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(materialsHashDate);
                    Integer materialBuyVol = orderBookRepository.getMaterialBuyVolume(startDate);
                    Integer materialSellVol = orderBookRepository.getMaterialSellVolume(startDate);
                    Long metarialTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.METARIALS), startDate);

                    // construction
                    String constructionPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(constructionHashDate);
                    Integer constructionBuyVol = orderBookRepository.getConstructionBuyVolume(startDate);
                    Integer constructionSellVol = orderBookRepository.getConstructionSellVolume(startDate);
                    Long constructionTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.CONSTRUCTION), startDate);

                    // electric
                    String electricPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(eletricHashDate);
                    Integer electricBuyVol = orderBookRepository.getElectricBuyVolume(startDate);
                    Integer electricSellVol = orderBookRepository.getElectricSellVolume(startDate);
                    Long electricTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.ELECTRIC), startDate);

                    // chemistry
                    String chemistryPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(chemistryHashDate);
                    Integer chemistryBuyVol = orderBookRepository.getChemistryBuyVolume(startDate);
                    Integer chemistrySellVol = orderBookRepository.getChemistrySellVolume(startDate);
                    Long chemistryTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.CHEMISTRY_FERTILIZER), startDate);

                    // animals
                    String animalsPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(animalsHashDate);
                    Integer animalsBuyVol = orderBookRepository.getAnimalsBuyVolume(startDate);
                    Integer animalsSellVol = orderBookRepository.getAnimalsSellVolume(startDate);
                    Long animalTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.ANIMALS), startDate);

                    // insurance
                    String insurancePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(insuranceHashDate);
                    Integer insuranceBuyVol = orderBookRepository.getInsuranceBuyVolume(startDate);
                    Integer insuranceSellVol = orderBookRepository.getInsuranceSellVolume(startDate);
                    Long insuranceTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.INSURANCE), startDate);

                    // airline
                    String airlinePercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(airlineHashDate);
                    Integer airlineBuyVol = orderBookRepository.getAirlineBuyVolume(startDate);
                    Integer airlineSellVol = orderBookRepository.getAirlineSellVolume(startDate);
                    Long airlineTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.AIRLINE), startDate);

                    // plastic
                    String plasticPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(plasticHashDate);
                    Integer plasticBuyVol = orderBookRepository.getPlasticBuyVolume(startDate);
                    Integer plasticSellVol = orderBookRepository.getPlasticSellVolume(startDate);
                    Long plasticTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.PLASTIC), startDate);

                    // tech
                    String techPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(techHashDate);
                    Integer techBuyVol = orderBookRepository.getTechBuyVolume(startDate);
                    Integer techSellVol = orderBookRepository.getTechSellVolume(startDate);
                    Long techTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.TECH), startDate);

                    // pharma
                    String pharmaPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(pharmaHashDate);
                    Integer pharmaBuyVol = orderBookRepository.getBuyVolume(Arrays.asList(IndustryConstant.PHARMACEUTICAL), startDate);
                    Integer pharmaSellVol = orderBookRepository.getSellVolume(Arrays.asList(IndustryConstant.PHARMACEUTICAL), startDate);
                    Long pharmaTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.PHARMACEUTICAL), startDate);

                    // sugar 
                    String sugarPercentage = indexStatisticRepository.findPercentageTakenOnIndexByHashDate(sugarHashDate);
                    Integer sugarBuyVol = orderBookRepository.getBuyVolume(Arrays.asList(IndustryConstant.SUGAR), startDate);
                    Integer sugarSellVol = orderBookRepository.getSellVolume(Arrays.asList(IndustryConstant.SUGAR), startDate);
                    Long sugarTotalVol = stockPriceRepository.getTotalVolumeSum(List.of(IndustryConstant.SUGAR), startDate);

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
                    String pharmaString = pharmaPercentage.replace("%", "");
                    String sugarString = sugarPercentage.replace("%", "");

                    excelHelper.updateCellDate(workbook, row, 1, tradingDate);
                    excelHelper.updateCellDouble(workbook, row, 2, Double.parseDouble(vn30String) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 3, Double.parseDouble(bluechipString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 4, Double.parseDouble(midcapString) / 100, true);
                    excelHelper.updateCellDouble(workbook, row, 5, Double.parseDouble(smallcapString) / 100, true);

                    excelHelper.updateCellDouble(workbook, row, 6,Double.parseDouble(bankString) / 100,true);
                    excelHelper.updateCellInt(workbook, row, 7, bankBuyVol);
                    excelHelper.updateCellInt(workbook, row, 8, bankSellVol);
                    excelHelper.updateCellInt(workbook, row, 9, bankTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 10, Double.parseDouble(stockString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 11, stockBuyVol);
                    excelHelper.updateCellInt(workbook, row, 12, stockSellVol);
                    excelHelper.updateCellInt(workbook, row, 13, stockTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 14, Double.parseDouble(bdsString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 15, bdsBuyVol);
                    excelHelper.updateCellInt(workbook, row, 16, bdsSellVol);
                    excelHelper.updateCellInt(workbook, row, 17, bdsTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 18, Double.parseDouble(kcnString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 19, kcnBuyVol);
                    excelHelper.updateCellInt(workbook, row, 20, kcnSellVol);
                    excelHelper.updateCellInt(workbook, row, 21, kcnTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 22, Double.parseDouble(steelString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 23, steelBuyVol);
                    excelHelper.updateCellInt(workbook, row, 24, steelSellVol);
                    excelHelper.updateCellInt(workbook, row, 25, steelTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 26, Double.parseDouble(retailString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 27, retailBuyVol);
                    excelHelper.updateCellInt(workbook, row, 28, retailSellVol);
                    excelHelper.updateCellInt(workbook, row, 29, retailTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 30, Double.parseDouble(oilString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 31, oilBuyVol);
                    excelHelper.updateCellInt(workbook, row, 32, oilSellVol);
                    excelHelper.updateCellInt(workbook, row, 33, oilTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 34, Double.parseDouble(constructionString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 35, constructionBuyVol);
                    excelHelper.updateCellInt(workbook, row, 36, constructionSellVol);
                    excelHelper.updateCellInt(workbook, row, 37, constructionTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 38, Double.parseDouble(logisticsString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 39, logisticBuyVol);
                    excelHelper.updateCellInt(workbook, row, 40, logisticSellVol);
                    excelHelper.updateCellInt(workbook, row, 41, logisticTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 42, Double.parseDouble(textileString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 43, textileBuyVol);
                    excelHelper.updateCellInt(workbook, row, 44, textileSellVol);
                    excelHelper.updateCellInt(workbook, row, 45, textileTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 46, Double.parseDouble(seafoodString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 47, seafoodBuyVol);
                    excelHelper.updateCellInt(workbook, row, 48, seafoodSellVol);
                    excelHelper.updateCellInt(workbook, row, 49, seafoodTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 50, Double.parseDouble(woodString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 51, woodBuyVol);
                    excelHelper.updateCellInt(workbook, row, 52, woodSellVol);
                    excelHelper.updateCellInt(workbook, row, 53, woodTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 54, Double.parseDouble(materialString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 55, materialBuyVol);
                    excelHelper.updateCellInt(workbook, row, 56, materialSellVol);
                    excelHelper.updateCellInt(workbook, row, 57, metarialTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 58, Double.parseDouble(electricString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 59, electricBuyVol);
                    excelHelper.updateCellInt(workbook, row, 60, electricSellVol);
                    excelHelper.updateCellInt(workbook, row, 61, electricTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 62, Double.parseDouble(chemistryString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 63, chemistryBuyVol);
                    excelHelper.updateCellInt(workbook, row, 64, chemistrySellVol);
                    excelHelper.updateCellInt(workbook, row, 65, chemistryTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 66, Double.parseDouble(animalsString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 67, animalsBuyVol);
                    excelHelper.updateCellInt(workbook, row, 68, animalsSellVol);
                    excelHelper.updateCellInt(workbook, row, 69, animalTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 70, Double.parseDouble(insuranceString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 71, insuranceBuyVol);
                    excelHelper.updateCellInt(workbook, row, 72, insuranceSellVol);
                    excelHelper.updateCellInt(workbook, row, 73, insuranceTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 74, Double.parseDouble(airlineString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 75, airlineBuyVol);
                    excelHelper.updateCellInt(workbook, row, 76, airlineSellVol);
                    excelHelper.updateCellInt(workbook, row, 77, airlineTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 78, Double.parseDouble(plasticString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 79, plasticBuyVol);
                    excelHelper.updateCellInt(workbook, row, 80, plasticSellVol);
                    excelHelper.updateCellInt(workbook, row, 81, plasticTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 82, Double.parseDouble(techString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 83, techBuyVol);
                    excelHelper.updateCellInt(workbook, row, 84, techSellVol);
                    excelHelper.updateCellInt(workbook, row, 85, techTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 86, Double.parseDouble(pharmaString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 87, pharmaBuyVol);
                    excelHelper.updateCellInt(workbook, row, 88, pharmaSellVol);
                    excelHelper.updateCellInt(workbook, row, 89, pharmaTotalVol.intValue());

                    excelHelper.updateCellDouble(workbook, row, 90, Double.parseDouble(sugarString) / 100, true);
                    excelHelper.updateCellInt(workbook, row, 91, sugarBuyVol);
                    excelHelper.updateCellInt(workbook, row, 92, sugarSellVol);
                    excelHelper.updateCellInt(workbook, row, 93, sugarTotalVol.intValue());
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
    public void writeForeignTopBuySell(String tradingDate, Boolean isLastDayOfWeek) {
        LocalDate date = LocalDate.parse(tradingDate);

        // Foreign buy/sell
        List<ForeignTradingEntity> topForeignBuy = foreignTradingRepository.getTop12DailyBuy(date);
        for (ForeignTradingEntity entity : topForeignBuy){
            ForeignTradingStatisticEntity statistic = foreignTradingStatisticRepository.findBySymbol(entity.getSymbol());
            if(statistic.getHighestBuyValue() != null && statistic.getHighestSellValue() != 0){
                if(entity.getTotalNetValue() >= statistic.getHighestBuyValue())
                    entity.setBiggestATH(true);
                else if(entity.getTotalNetValue() >= statistic.getSixMonthsHighestBuyValue()){
                    entity.setBiggest6M(true);
                }
            }
        }

        List<ForeignTradingEntity> topForeignSell = foreignTradingRepository.getTop12DailySell(date);
        for (ForeignTradingEntity entity : topForeignSell){
            ForeignTradingStatisticEntity statistic = foreignTradingStatisticRepository.findBySymbol(entity.getSymbol());
            if(statistic.getHighestSellValue() != null && statistic.getHighestSellValue() != 0){
                if(entity.getTotalNetValue() <= statistic.getHighestSellValue())
                    entity.setSmallestATH(true);
                else if(entity.getTotalNetValue() <= statistic.getSixMonthsHighestSellValue()){
                    entity.setSmallest6M(true);
                }
            }
        }

        excelHelper.writeDailyTopTradeToFile(StockConstant.NN_MUA_SHEET, topForeignBuy,true, tradingDate);
        excelHelper.writeDailyTopTradeToFile(StockConstant.NN_MUA_SHEET, topForeignBuy,false, tradingDate);
        excelHelper.writeDailyTopTradeToFile(StockConstant.NN_BAN_SHEET, topForeignSell,true, tradingDate);
        excelHelper.writeDailyTopTradeToFile(StockConstant.NN_BAN_SHEET, topForeignSell,false, tradingDate);

        excelHelper.highlightTopTrade(StockConstant.NN_MUA_SHEET, topForeignBuy);
        excelHelper.highlightTopTrade(StockConstant.NN_BAN_SHEET, topForeignSell);

        TradingDateEntity dateTrading = tradingDateRepository.getTradingDateEntityByTradingDate(date);
        List<TradingDateEntity> dateOfWeek = tradingDateRepository.getTradingDateEntitiesByTradingWeek(dateTrading.getTradingWeek());

        if(Boolean.TRUE.equals(isLastDayOfWeek)){
            Optional<TradingDateEntity> lastDayOfWeek = dateOfWeek.stream()
                    .max(Comparator.comparing(TradingDateEntity::getId));

            if(lastDayOfWeek.isPresent()){
                if(Objects.equals(dateTrading.getId(), lastDayOfWeek.get().getId())){
                    List<LocalDate> dateOfWeeks = dateOfWeek.stream().map(TradingDateEntity::getTradingDate).collect(Collectors.toList());
                    List<SymbolTotalNetValueDTO> nnWeeklyBuy = foreignTradingRepository.getTop12WeeklyBuy(dateOfWeeks.get(0), dateOfWeeks.get(dateOfWeeks.size() - 1));
                    List<SymbolTotalNetValueDTO> nnWeeklySell = foreignTradingRepository.getTop12WeeklySell(dateOfWeeks.get(0), dateOfWeeks.get(dateOfWeeks.size() - 1));

                    excelHelper.writeTopWeekTradeToFile(StockConstant.NN_MUA_SHEET, nnWeeklyBuy, true, dateTrading.getTradingWeek());
                    excelHelper.writeTopWeekTradeToFile(StockConstant.NN_MUA_SHEET, nnWeeklyBuy, false, dateTrading.getTradingWeek());
                    excelHelper.mergeCell(StockConstant.NN_MUA_SHEET, 1, 2, 13, 13);

                    excelHelper.writeTopWeekTradeToFile(StockConstant.NN_BAN_SHEET, nnWeeklySell, true, dateTrading.getTradingWeek());
                    excelHelper.writeTopWeekTradeToFile(StockConstant.NN_BAN_SHEET, nnWeeklySell, false, dateTrading.getTradingWeek());
                    excelHelper.mergeCell(StockConstant.NN_BAN_SHEET, 1, 2, 13, 13);

                    log.info("Hoan thanh thong ke tuan");
                }
            }
        }

        log.info("Hoan thanh format mua ban ki luc");
    }

    @Override
    public void writeProprietaryTopBuySell(String tradingDate, Boolean isLastDayOfWeek) {
        LocalDate date = LocalDate.parse(tradingDate);

        // Proprietary buy/sell
        List<ProprietaryTradingEntity> topProprietaryBuy = proprietaryTradingRepository.getTop12DailyBuy(date);
        for (ProprietaryTradingEntity entity : topProprietaryBuy){
            log.error("Xy ly mck. {}", entity.getSymbol());
            ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(entity.getSymbol());

            // no data => no highlight
            if(statistic.getHighestBuyValue() != null){
                if(entity.getTotalNetValue() >= statistic.getHighestBuyValue() && statistic.getHighestBuyValue() != 0)
                    entity.setBiggestATH(true);
                else if(entity.getTotalNetValue() >= statistic.getSixMonthsHighestBuyValue() && statistic.getSixMonthsHighestBuyValue() != 0)
                    entity.setBiggest6M(true);
            }
        }

        List<ProprietaryTradingEntity> topProprietarySell = proprietaryTradingRepository.getTop12DailySell(date);
        for (ProprietaryTradingEntity entity : topProprietarySell){
            ProprietaryTradingStatisticEntity statistic = proprietaryTradingStatisticRepository.findBySymbol(entity.getSymbol());
            // no data => no highlight
            if(statistic.getHighestSellValue() != null ){
                if(entity.getTotalNetValue() <= statistic.getHighestSellValue() && statistic.getHighestSellValue() != 0)
                    entity.setSmallestATH(true);
                else if(entity.getTotalNetValue() <= statistic.getSixMonthsHighestSellValue() && statistic.getSixMonthsHighestSellValue() != 0)
                    entity.setSmallest6M(true);
            }
        }

        excelHelper.writeDailyTopTradeToFile(StockConstant.TD_MUA_SHEET, topProprietaryBuy,true, tradingDate);
        excelHelper.writeDailyTopTradeToFile(StockConstant.TD_MUA_SHEET, topProprietaryBuy,false, tradingDate);
        excelHelper.writeDailyTopTradeToFile(StockConstant.TD_BAN_SHEET, topProprietarySell,true, tradingDate);
        excelHelper.writeDailyTopTradeToFile(StockConstant.TD_BAN_SHEET, topProprietarySell,false, tradingDate);

        excelHelper.highlightTopTrade(StockConstant.TD_MUA_SHEET, topProprietaryBuy);
        excelHelper.highlightTopTrade(StockConstant.TD_BAN_SHEET, topProprietarySell);

        TradingDateEntity dateTrading = tradingDateRepository.getTradingDateEntityByTradingDate(date);
        List<TradingDateEntity> dateOfWeek = tradingDateRepository.getTradingDateEntitiesByTradingWeek(dateTrading.getTradingWeek());

        if(Boolean.TRUE.equals(isLastDayOfWeek)){
            Optional<TradingDateEntity> lastDayOfWeek = dateOfWeek.stream()
                    .max(Comparator.comparing(TradingDateEntity::getId));

            if(lastDayOfWeek.isPresent()){
                if(Objects.equals(dateTrading.getId(), lastDayOfWeek.get().getId())){
                    List<LocalDate> dateOfWeeks = dateOfWeek.stream().map(TradingDateEntity::getTradingDate).collect(Collectors.toList());
                    List<SymbolTotalNetValueDTO> tdWeeklyBuy = proprietaryTradingRepository.getTop12WeeklyBuy(dateOfWeeks.get(0), dateOfWeeks.get(dateOfWeeks.size() - 1));
                    List<SymbolTotalNetValueDTO> tdWeeklySell = proprietaryTradingRepository.getTop12WeeklySell(dateOfWeeks.get(0), dateOfWeeks.get(dateOfWeeks.size() - 1));

                    excelHelper.writeTopWeekTradeToFile(StockConstant.TD_MUA_SHEET, tdWeeklyBuy, true, dateTrading.getTradingWeek());
                    excelHelper.writeTopWeekTradeToFile(StockConstant.TD_MUA_SHEET, tdWeeklyBuy, false, dateTrading.getTradingWeek());
                    excelHelper.mergeCell(StockConstant.TD_MUA_SHEET, 1, 2, 13, 13);

                    excelHelper.writeTopWeekTradeToFile(StockConstant.TD_BAN_SHEET, tdWeeklySell, true, dateTrading.getTradingWeek());
                    excelHelper.writeTopWeekTradeToFile(StockConstant.TD_BAN_SHEET, tdWeeklySell, false, dateTrading.getTradingWeek());
                    excelHelper.mergeCell(StockConstant.TD_BAN_SHEET, 1, 2, 13, 13);

                    log.info("Hoan thanh thong ke tuan");
                }
            }
        }

        log.info("Hoan thanh format mua ban ki luc");
    }

    @Override
    public void analyzeOrderBook(String tradingDate) {
        LocalDate date = LocalDate.parse(tradingDate);
        List<OrderBookEntity> orderbooks = orderBookRepository.getOrderBookEntitiesByTradingDate(date);

        Integer smallBuyClx = getExcelColumnIndex(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX);
        Integer smallSellClx = getExcelColumnIndex(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX);
        Integer mediumBuyClx = getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_BUY_ORDER_COLUMN_INDEX);
        Integer mediumSellClx = getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_SELL_ORDER_COLUMN_INDEX);
        Integer largeBuyClx = getExcelColumnIndex(StockConstant.INTRADAY_LARGE_BUY_ORDER_COLUMN_INDEX);
        Integer largeSellClx = getExcelColumnIndex(StockConstant.INTRADAY_LARGE_SELL_ORDER_COLUMN_INDEX);
        Integer buyVolClx = getExcelColumnIndex(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX);
        Integer sellVolClx = getExcelColumnIndex(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX);
        Integer atcVolClx = getExcelColumnIndex(StockConstant.INTRADAY_ATC_VOLUME_COLUMN_INDEX);
        Integer percentChangeClx = getExcelColumnIndex(StockConstant.INTRADAY_PERCENTAGE_CHANGE_COLUMN_INDEX);
        Integer volClx = getExcelColumnIndex(StockConstant.INTRADAY_TOTAL_VOLUME_COLUMN_INDEX);

        try (FileInputStream fileInputStream = new FileInputStream(orderbookFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            ZipSecureFile.setMinInflateRatio(0);

            for (OrderBookEntity entity : orderbooks) {
                Sheet sheet = workbook.getSheet(entity.getSymbol());
                if(sheet == null) {
                    log.info("Sheet {} not exist", entity.getSymbol());
                    continue;
                }

                excelHelper.insertNewRow(sheet, 2);
                Row updateRow = sheet.getRow(2);

                excelHelper.updateCellDate(workbook, updateRow, 1, entity.getTradingDate().toString());
                excelHelper.updateCellDouble(workbook, updateRow, smallBuyClx, Double.valueOf(entity.getBuyOrder()), false);
                excelHelper.updateCellDouble(workbook, updateRow, smallSellClx, Double.valueOf(entity.getSellOrder()),false);
                excelHelper.updateCellDouble(workbook, updateRow, mediumBuyClx, Double.valueOf(entity.getMediumBuyOrder()), false);
                excelHelper.updateCellDouble(workbook, updateRow, mediumSellClx, Double.valueOf(entity.getMediumSellOrder()), false);
                excelHelper.updateCellDouble(workbook, updateRow, largeBuyClx, Double.valueOf(entity.getLargeBuyOrder()), false);
                excelHelper.updateCellDouble(workbook, updateRow, largeSellClx, Double.valueOf(entity.getLargeSellOrder()), false);
                excelHelper.updateCellDouble(workbook, updateRow, buyVolClx, Double.valueOf(entity.getBuyVolume()), false);
                excelHelper.updateCellDouble(workbook, updateRow, sellVolClx, Double.valueOf(entity.getSellVolume()), false);
                excelHelper.updateCellDouble(workbook, updateRow, atcVolClx, Double.valueOf(entity.getAtc_volume()), false);
                StockPriceEntity stockPriceEntity = stockPriceRepository.getPercentageChangeByTradingDate(date, entity.getSymbol());
                excelHelper.updateCellString(workbook, updateRow, percentChangeClx, stockPriceEntity.getPercentageChange().replace(".", ","));
                excelHelper.updateCellDouble(workbook, updateRow, volClx, stockPriceEntity.getTotalVolume(), false);
            }

            try (FileOutputStream fileOut = new FileOutputStream(orderbookFile)) {
                workbook.write(fileOut);
                log.info("ghi file thanh cong.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", orderbookFile);
        }
    }

    @Override
    public void writeOrderBookFromDateToDate(String symbol, String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        Long startId = tradingDateRepository.getIdByTradingDate(startDate);
        Long endId = tradingDateRepository.getIdByTradingDate(endDate);
        if(startId != null && endId != null){
            for (long i = startId; i <= endId; i++) {
                Optional<TradingDateEntity> tradingDate = tradingDateRepository.findById(i);
                if (tradingDate.isPresent()) {
                    OrderBookEntity orderbook = orderBookRepository.findOrderBookEntitiesBySymbolAndTradingDate(symbol, tradingDate.get().getTradingDate());
                    if(orderbook != null){
                        log.info("Ghi du lieu cho ma ck {} ngay {}", symbol, tradingDate.get().getTradingDate());
                        Integer smallBuyClx = getExcelColumnIndex(StockConstant.INTRADAY_BUY_ORDER_COLUMN_INDEX);
                        Integer smallSellClx = getExcelColumnIndex(StockConstant.INTRADAY_SELL_ORDER_COLUMN_INDEX);
                        Integer mediumBuyClx = getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_BUY_ORDER_COLUMN_INDEX);
                        Integer mediumSellClx = getExcelColumnIndex(StockConstant.INTRADAY_MEDIUM_SELL_ORDER_COLUMN_INDEX);
                        Integer largeBuyClx = getExcelColumnIndex(StockConstant.INTRADAY_LARGE_BUY_ORDER_COLUMN_INDEX);
                        Integer largeSellClx = getExcelColumnIndex(StockConstant.INTRADAY_LARGE_SELL_ORDER_COLUMN_INDEX);
                        Integer buyVolClx = getExcelColumnIndex(StockConstant.INTRADAY_BUY_VOLUME_COLUMN_INDEX);
                        Integer sellVolClx = getExcelColumnIndex(StockConstant.INTRADAY_SELL_VOLUME_COLUMN_INDEX);
                        Integer atcVolClx = getExcelColumnIndex(StockConstant.INTRADAY_ATC_VOLUME_COLUMN_INDEX);
                        Integer percentChangeClx = getExcelColumnIndex(StockConstant.INTRADAY_PERCENTAGE_CHANGE_COLUMN_INDEX);
                        Integer volClx = getExcelColumnIndex(StockConstant.INTRADAY_TOTAL_VOLUME_COLUMN_INDEX);

                        try (FileInputStream fileInputStream = new FileInputStream(orderbookFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
                            ZipSecureFile.setMinInflateRatio(0);

                                Sheet sheet = workbook.getSheet(symbol);
                                if(sheet == null) {
                                    log.info("Sheet {} not exist", symbol);
                                    throw new RuntimeException("Sheet not exist");
                                }

                                excelHelper.insertNewRow(sheet, 2);
                                Row updateRow = sheet.getRow(2);

                                excelHelper.updateCellDate(workbook, updateRow, 1, orderbook.getTradingDate().toString());
                                excelHelper.updateCellDouble(workbook, updateRow, smallBuyClx, Double.valueOf(orderbook.getBuyOrder()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, smallSellClx, Double.valueOf(orderbook.getSellOrder()),false);
                                excelHelper.updateCellDouble(workbook, updateRow, mediumBuyClx, Double.valueOf(orderbook.getMediumBuyOrder()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, mediumSellClx, Double.valueOf(orderbook.getMediumSellOrder()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, largeBuyClx, Double.valueOf(orderbook.getLargeBuyOrder()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, largeSellClx, Double.valueOf(orderbook.getLargeSellOrder()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, buyVolClx, Double.valueOf(orderbook.getBuyVolume()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, sellVolClx, Double.valueOf(orderbook.getSellVolume()), false);
                                excelHelper.updateCellDouble(workbook, updateRow, atcVolClx, Double.valueOf(orderbook.getAtc_volume()), false);
                                StockPriceEntity stockPriceEntity = stockPriceRepository.getPercentageChangeByTradingDate(tradingDate.get().getTradingDate(), symbol);
                                excelHelper.updateCellString(workbook, updateRow, percentChangeClx, stockPriceEntity.getPercentageChange().replace(".", ","));
                                excelHelper.updateCellDouble(workbook, updateRow, volClx, stockPriceEntity.getTotalVolume(), false);

                            try (FileOutputStream fileOut = new FileOutputStream(orderbookFile)) {
                                workbook.write(fileOut);
                                log.info("ghi file thanh cong.");
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            log.error("Loi trong qua trinh xu ly file. {}", orderbookFile);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateIntradayData(String tradingDate, String updatedColumn) {
        LocalDate date = LocalDate.parse(tradingDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String findDate = date.format(formatter);

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));
            // find row index by trading date
            int rowUpdated = excelHelper.findRowIndexByCellValue(statisticFile, "Intraday", tradingDateIdx, 3, 200, findDate);
            if(rowUpdated != -1){
                Sheet sheet = workbook.getSheet("Intraday");
                Row row = sheet.getRow(rowUpdated);
                switch (updatedColumn){
                    case "order-book":
                        // find data  by trading date and symbol
                        int strongBuy = orderBookRepository.strongBuy(date);
                        int strongSell = orderBookRepository.strongSell(date);
                        excelHelper.updateCellInt(workbook, row, 12, strongBuy);
                        excelHelper.updateCellInt(workbook, row, 13, strongSell);
                        break;

                    case "high-low-close-price":
                        // find data  by trading date and symbol
                        int highClose = stockPriceRepository.getNumberOfHighEqualClose(date);
                        int lowClose = stockPriceRepository.getNumberOfLowEqualClose(date);
                        excelHelper.updateCellInt(workbook, row, 14, highClose);
                        excelHelper.updateCellInt(workbook, row, 15, lowClose);
                        break;

                    case "proprietary-buy-sell":
                        // find data  by trading date and symbol
                        int proprietaryBuy = proprietaryTradingRepository.getNumberOfBuy(date);
                        int proprietarySell = proprietaryTradingRepository.getNumberOfSell(date);
                        excelHelper.updateCellInt(workbook, row, 22, proprietaryBuy);
                        excelHelper.updateCellInt(workbook, row, 23, proprietarySell);
                        break;
                    default:
                        log.info("Khong tim thay gia tri cot tuong ung cua {}", updatedColumn);
                        break;
                }
            } else {
                log.info("Khong tim thay gia tri ngay tuong ung cua {}", tradingDate);
                throw new RuntimeException("Khong tim thay gia tri ngay tuong ung cua");
            }

            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("ghi file thanh cong.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }
    }

    @Override
    public void updateMoneyFlowData(String tradingDate, String updatedColumn) {
        LocalDate date = LocalDate.parse(tradingDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String findDate = date.format(formatter);

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));
            // find row index by trading date
            int rowUpdated = excelHelper.findRowIndexByCellValue(statisticFile, "MoneyFlow", tradingDateIdx, 3, 200, findDate);
            if(rowUpdated != -1){
                Sheet sheet = workbook.getSheet("MoneyFlow");
                Row row = sheet.getRow(rowUpdated);
                switch (updatedColumn){
                    case "bank":
                        Double totalVol = stockPriceRepository.findTotalVolumeBySymbolAndTradingDate("VNINDEX", date);

                        // banks
//                        String[] banks = IndustryConstant.BANKS;
//                        long banksVol = stockPriceRepository.getTotalVolumeSum(List.of(banks), date);
//                        Integer bankBuyVol = orderBookRepository.getBankBuyVolume(date);
//                        Integer banksSellVol = orderBookRepository.getBankSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 6, banksVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 7, bankBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 8, banksSellVol);
//                        excelHelper.updateCellInt(workbook, row, 9, (int) banksVol);
//                        log.info("cap nhat ty trong bank thanh cong.");

                        // stocks
//                        String[] stocks = IndustryConstant.STOCKS;
//                        long stocksVol = stockPriceRepository.getTotalVolumeSum(List.of(stocks), date);
//                        Integer stockBuyVol = orderBookRepository.getStockBuyVolume(date);
//                        Integer stockSellVol = orderBookRepository.getStockSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 10, stocksVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 11, stockBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 12, stockSellVol);
//                        excelHelper.updateCellInt(workbook, row, 13, (int) stocksVol);
//                        log.info("cap nhat ty trong stock thanh cong.");

                        // bds
//                        String[] bds = IndustryConstant.BDS;
//                        long bdsVol = stockPriceRepository.getTotalVolumeSum(List.of(bds), date);
//                        Integer bdsBuyVol = orderBookRepository.getRealEstateBuyVolume(date);
//                        Integer bdsSellVol = orderBookRepository.getRealEstateSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 14, bdsVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 15, bdsBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 16, bdsSellVol);
//                        excelHelper.updateCellInt(workbook, row, 17, (int) bdsVol);
//                        log.info("cap nhat ty trong bds thanh cong.");

                        // kcn
//                        String[] kcn = IndustryConstant.BDS_KCN;
//                        long kcnVol = stockPriceRepository.getTotalVolumeSum(List.of(kcn), date);
//                        Integer kcnBuyVol = orderBookRepository.getKCNBuyVolume(date);
//                        Integer kcnSellVol = orderBookRepository.getKCNSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 18, kcnVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 19, kcnBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 20, kcnSellVol);
//                        excelHelper.updateCellInt(workbook, row, 21, (int) kcnVol);
//                        log.info("cap nhat ty trong kcn thanh cong.");

                        // steel
//                        String[] steel = IndustryConstant.STEELS;
//                        long steelVol = stockPriceRepository.getTotalVolumeSum(List.of(steel), date);
//                        Integer steelBuyVol = orderBookRepository.getSteelBuyVolume(date);
//                        Integer steelSellVol = orderBookRepository.getSteelSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 22, steelVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 23, steelBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 24, steelSellVol);
//                        excelHelper.updateCellInt(workbook, row, 25, (int) steelVol);
//                        log.info("cap nhat ty trong steel thanh cong.");

                        // retail
//                        String[] retail = IndustryConstant.RETAIL;
//                        long retailVol = stockPriceRepository.getTotalVolumeSum(List.of(retail), date);
//                        Integer retailBuyVol = orderBookRepository.getRetailsBuyVolume(date);
//                        Integer retailSellVol = orderBookRepository.getRetailsSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 26, retailVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 27, retailBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 28, retailSellVol);
//                        excelHelper.updateCellInt(workbook, row, 29, (int) retailVol);
//                        log.info("cap nhat ty trong retail thanh cong.");

                        // oil
//                        String[] oil = IndustryConstant.OIL_GAS;
//                        long oilVol = stockPriceRepository.getTotalVolumeSum(List.of(oil), date);
//                        Integer oilBuyVol = orderBookRepository.getOilBuyVolume(date);
//                        Integer oilSellVol = orderBookRepository.getOilSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 30, oilVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 31, oilBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 32, oilSellVol);
//                        excelHelper.updateCellInt(workbook, row, 33, (int) oilVol);
//                        log.info("cap nhat ty trong oil thanh cong.");

                        // constructions
//                        String[] constructions = IndustryConstant.CONSTRUCTION;
//                        long constructionsVol = stockPriceRepository.getTotalVolumeSum(List.of(constructions), date);
//                        Integer constructionsBuyVol = orderBookRepository.getConstructionBuyVolume(date);
//                        Integer constructionsSellVol = orderBookRepository.getConstructionSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 34, constructionsVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 35, constructionsBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 36, constructionsSellVol);
//                        excelHelper.updateCellInt(workbook, row, 37, (int) constructionsVol);
//                        log.info("cap nhat ty trong constructions thanh cong.");

                        // logistics
//                        String[] logistics = IndustryConstant.LOGISTICS;
//                        long logisticsVol = stockPriceRepository.getTotalVolumeSum(List.of(logistics), date);
//                        Integer logisticsBuyVol = orderBookRepository.getLogisticBuyVolume(date);
//                        Integer logisticsSellVol = orderBookRepository.getLogisticSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 38, logisticsVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 39, logisticsBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 40, logisticsSellVol);
//                        excelHelper.updateCellInt(workbook, row, 41, (int) logisticsVol);
//                        log.info("cap nhat ty trong logistics thanh cong.");

                        // textile
//                        String[] textile = IndustryConstant.TEXTILE;
//                        long textileVol = stockPriceRepository.getTotalVolumeSum(List.of(textile), date);
//                        Integer textileBuyVol = orderBookRepository.getTextileBuyVolume(date);
//                        Integer textileSellVol = orderBookRepository.getTextileSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 42, textileVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 43, textileBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 44, textileSellVol);
//                        excelHelper.updateCellInt(workbook, row, 45, (int) textileVol);
//                        log.info("cap nhat ty trong textile thanh cong.");

                        // seafood
//                        String[] seafood = IndustryConstant.SEAFOOD;
//                        long seafoodVol = stockPriceRepository.getTotalVolumeSum(List.of(seafood), date);
//                        Integer seafoodBuyVol = orderBookRepository.getSeafoodBuyVolume(date);
//                        Integer seafoodSellVol = orderBookRepository.getSeafoodSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 46, seafoodVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 47, seafoodBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 48, seafoodSellVol);
//                        excelHelper.updateCellInt(workbook, row, 49, (int) seafoodVol);
//                        log.info("cap nhat ty trong seafood thanh cong.");

                        // wood
//                        String[] wood = IndustryConstant.WOOD;
//                        long woodVol = stockPriceRepository.getTotalVolumeSum(List.of(wood), date);
//                        Integer woodBuyVol = orderBookRepository.getWoodBuyVolume(date);
//                        Integer woodSellVol = orderBookRepository.getWoodSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 50, woodVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 51, woodBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 52, woodSellVol);
//                        excelHelper.updateCellInt(workbook, row, 53, (int) woodVol);
//                        log.info("cap nhat ty trong wood thanh cong.");

                        // materials
//                        String[] materials = IndustryConstant.METARIALS;
//                        long materialsVol = stockPriceRepository.getTotalVolumeSum(List.of(materials), date);
//                        Integer materialsBuyVol = orderBookRepository.getMaterialBuyVolume(date);
//                        Integer materialsSellVol = orderBookRepository.getMaterialSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 54, materialsVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 55, materialsBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 56, materialsSellVol);
//                        excelHelper.updateCellInt(workbook, row, 57, (int) materialsVol);
//                        log.info("cap nhat ty trong materials thanh cong.");

                        // electric
//                        String[] electric = IndustryConstant.ELECTRIC;
//                        long electricVol = stockPriceRepository.getTotalVolumeSum(List.of(electric), date);
//                        Integer electricBuyVol = orderBookRepository.getElectricBuyVolume(date);
//                        Integer electricSellVol = orderBookRepository.getElectricSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 58, electricVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 59, electricBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 60, electricSellVol);
//                        excelHelper.updateCellInt(workbook, row, 61, (int) electricVol);
//                        log.info("cap nhat ty trong electric thanh cong.");

                        // chemistry
//                        String[] chemistry = IndustryConstant.CHEMISTRY_FERTILIZER;
//                        long chemistryVol = stockPriceRepository.getTotalVolumeSum(List.of(chemistry), date);
//                        Integer chemistryBuyVol = orderBookRepository.getChemistryBuyVolume(date);
//                        Integer chemistrySellVol = orderBookRepository.getChemistrySellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 62, chemistryVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 63, chemistryBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 64, chemistrySellVol);
//                        excelHelper.updateCellInt(workbook, row, 65, (int) chemistryVol);
//                        log.info("cap nhat ty trong chemistry thanh cong.");

                        // animals
//                        String[] animals = IndustryConstant.ANIMALS;
//                        long animalsVol = stockPriceRepository.getTotalVolumeSum(List.of(animals), date);
//                        Integer animalsBuyVol = orderBookRepository.getAnimalsBuyVolume(date);
//                        Integer animalsSellVol = orderBookRepository.getAnimalsSellVolume(date);
//                        excelHelper.updateCellDouble(workbook, row, 66, animalsVol/totalVol, true);
//                        excelHelper.updateCellInt(workbook, row, 67, animalsBuyVol);
//                        excelHelper.updateCellInt(workbook, row, 68, animalsSellVol);
//                        excelHelper.updateCellInt(workbook, row, 69, (int) animalsVol);
//                        log.info("cap nhat ty trong animals thanh cong.");

                        // insurance
//                        Integer insuranceBuyVol = orderBookRepository.getInsuranceBuyVolume(date);
//                        if(insuranceBuyVol != null) {
//                            String[] insurance = IndustryConstant.INSURANCE;
//                            long insuranceVol = stockPriceRepository.getTotalVolumeSum(List.of(insurance), date);
//                            Integer insuranceSellVol = orderBookRepository.getInsuranceSellVolume(date);
//                            excelHelper.updateCellDouble(workbook, row, 70, insuranceVol/totalVol, true);
//                            excelHelper.updateCellInt(workbook, row, 71, insuranceBuyVol);
//                            excelHelper.updateCellInt(workbook, row, 72, insuranceSellVol);
//                            excelHelper.updateCellInt(workbook, row, 73, (int) insuranceVol);
//                            log.info("cap nhat ty trong insurance thanh cong.");
//                        }

                        // airlines
//                        Integer airlinesBuyVol = orderBookRepository.getAirlineBuyVolume(date);
//                        if(airlinesBuyVol != null) {
//                            String[] airlines = IndustryConstant.AIRLINE;
//                            long airlinesVol = stockPriceRepository.getTotalVolumeSum(List.of(airlines), date);
//                            Integer airlinesSellVol = orderBookRepository.getAirlineSellVolume(date);
//                            excelHelper.updateCellDouble(workbook, row, 74, airlinesVol/totalVol, true);
//                            excelHelper.updateCellInt(workbook, row, 75, airlinesBuyVol);
//                            excelHelper.updateCellInt(workbook, row, 76, airlinesSellVol);
//                            excelHelper.updateCellInt(workbook, row, 77, (int) airlinesVol);
//                            log.info("cap nhat ty trong airlines thanh cong.");
//                        }

                        // plastics
//                        Integer plasticsBuyVol = orderBookRepository.getPlasticBuyVolume(date);
//                        if(plasticsBuyVol != null){
//                            String[] plastics = IndustryConstant.PLASTIC;
//                            long plasticsVol = stockPriceRepository.getTotalVolumeSum(List.of(plastics), date);
//                            Integer plasticsSellVol = orderBookRepository.getPlasticSellVolume(date);
//                            excelHelper.updateCellDouble(workbook, row, 78, plasticsVol/totalVol, true);
//                            excelHelper.updateCellInt(workbook, row, 79, plasticsBuyVol);
//                            excelHelper.updateCellInt(workbook, row, 80, plasticsSellVol);
//                            excelHelper.updateCellInt(workbook, row, 81, (int) plasticsVol);
//                            log.info("cap nhat ty trong plastics thanh cong.");
//                        }


                        // tech
//                        Integer techBuyVol = orderBookRepository.getTechBuyVolume(date);
//                        if(techBuyVol != null){
//                            String[] tech = IndustryConstant.TECH;
//                            long techVol = stockPriceRepository.getTotalVolumeSum(List.of(tech), date);
//                            Integer techSellVol = orderBookRepository.getTechSellVolume(date);
//                            excelHelper.updateCellDouble(workbook, row, 82, techVol/totalVol, true);
//                            excelHelper.updateCellInt(workbook, row, 83, techBuyVol);
//                            excelHelper.updateCellInt(workbook, row, 84, techSellVol);
//                            excelHelper.updateCellInt(workbook, row, 85, (int) techVol);
//                            log.info("cap nhat ty trong tech thanh cong.");
//                        }

//                        String[] tech = IndustryConstant.TECH;
//                        long techVol = stockPriceRepository.getTotalVolumeSum(List.of(tech), date);
//                        excelHelper.updateCellInt(workbook, row, 85, (int) techVol);



                        // pharma
                        Integer pharmaBuyVol = orderBookRepository.getBuyVolume(List.of(IndustryConstant.PHARMACEUTICAL), date);
                        if(pharmaBuyVol != null){
                            String[] pharma = IndustryConstant.PHARMACEUTICAL;
                            long pharmaVol = stockPriceRepository.getTotalVolumeSum(List.of(pharma), date);
                            Integer pharmaSellVol = orderBookRepository.getSellVolume(List.of(IndustryConstant.PHARMACEUTICAL), date);
                            excelHelper.updateCellDouble(workbook, row, 86, pharmaVol/totalVol, true);
                            excelHelper.updateCellInt(workbook, row, 87, pharmaBuyVol);
                            excelHelper.updateCellInt(workbook, row, 88, pharmaSellVol);
//                            excelHelper.updateCellInt(workbook, row, 89, (int) pharmaVol);
                            log.info("cap nhat ty trong pharma thanh cong.");
                        }

                        // sugar
                        Integer sugarBuyVol = orderBookRepository.getBuyVolume(List.of(IndustryConstant.SUGAR), date);
                        if(sugarBuyVol != null){
                            String[] sugar = IndustryConstant.SUGAR;
                            long sugarVol = stockPriceRepository.getTotalVolumeSum(List.of(sugar), date);
                            Integer sugarSellVol = orderBookRepository.getSellVolume(List.of(IndustryConstant.SUGAR), date);
                            excelHelper.updateCellDouble(workbook, row, 90, sugarVol/totalVol, true);
                            excelHelper.updateCellInt(workbook, row, 91, sugarBuyVol);
                            excelHelper.updateCellInt(workbook, row, 92, sugarSellVol);
//                            excelHelper.updateCellInt(workbook, row, 93, (int) sugarVol);
                            log.info("cap nhat ty trong pharma thanh cong.");
                        }

                        break;

                    default:
                        log.info("Khong tim thay gia tri cot tuong ung cua {}", updatedColumn);
                        break;
                }
            } else {
                log.info("Khong tim row cua ngay {}", tradingDate);
                throw new RuntimeException("Khong tim row cua ngay " + tradingDate);
            }

            try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                workbook.write(fileOut);
                log.info("ghi file thanh cong.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
        }
    }

    @Override
    public void updateBuySellInVn30(String start, String end, String column) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        Long startId = tradingDateRepository.getIdByTradingDate(startDate);
        Long endId = tradingDateRepository.getIdByTradingDate(endDate);
        if(startId != null && endId != null){
            for (long i = startId; i <= endId ; i++) {
                TradingDateEntity dateTrading = tradingDateRepository.findById(i).get();
                String findDate = dateTrading.getTradingDate().format(formatter);
                ZipSecureFile.setMinInflateRatio(0);
                try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
                    int tradingDateIdx = Integer.parseInt(env.getProperty(StockConstant.TRADING_DATE_COLUMN_INDEX));
                    // find row index by trading date
                    int rowUpdated = excelHelper.findRowIndexByCellValue(statisticFile, "VN30", tradingDateIdx, 3, 200, findDate);
                    if (rowUpdated != -1) {
                        Sheet sheet = workbook.getSheet("VN30");
                        Row row = sheet.getRow(rowUpdated);
                        switch (column) {
                            case "buy-sell-in-vn30":
                                int proprietaryBuyInVn30 = proprietaryTradingRepository.getNumberOfBuyInVN30List(List.of(StockGroupsConstant.VN30), dateTrading.getTradingDate());
                                int proprietarySellInVn30 = proprietaryTradingRepository.getNumberOfSellInVN30List(List.of(StockGroupsConstant.VN30), dateTrading.getTradingDate());
                                int foreignBuyInVn30 = foreignTradingRepository.getNumberOfBuyInVN30List(List.of(StockGroupsConstant.VN30), dateTrading.getTradingDate());
                                int foreignSellInVn30 = foreignTradingRepository.getNumberOfSellInVN30List(List.of(StockGroupsConstant.VN30), dateTrading.getTradingDate());
                                excelHelper.updateCellInt(workbook, row, 9, proprietaryBuyInVn30);
                                excelHelper.updateCellInt(workbook, row, 10, proprietarySellInVn30);

                                excelHelper.updateCellInt(workbook, row, 13, foreignBuyInVn30);
                                excelHelper.updateCellInt(workbook, row, 14, foreignSellInVn30);
                                log.info("Cap nhat du lieu ngay {} thanh cong", dateTrading.getTradingDate());

                                break;

                            default:
                                log.info("Khong tim thay gia tri cot tuong ung cua {}", column);
                                break;
                        }
                    } else {
                        log.info("Khong tim thay gia tri ngay tuong ung cua {}", dateTrading.getTradingDate());
                        throw new RuntimeException("Khong tim thay gia tri ngay tuong ung cua");
                    }

                    try (FileOutputStream fileOut = new FileOutputStream(statisticFile)) {
                        workbook.write(fileOut);
                        log.info("ghi file thanh cong.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("Loi trong qua trinh xu ly file. {}", statisticFile);
                }
            }
        }

    }

}