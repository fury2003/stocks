package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.*;
import com.stock.cashflow.dto.IndexDTO;
import com.stock.cashflow.dto.IntradayDTO;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.IndexStatisticEntity;
import com.stock.cashflow.persistence.entity.StockPriceEntity;
import com.stock.cashflow.persistence.repository.ForeignTradingRepository;
import com.stock.cashflow.persistence.repository.IndexStatisticRepository;
import com.stock.cashflow.persistence.repository.ProprietaryTradingRepository;
import com.stock.cashflow.persistence.repository.StockPriceRepository;
import com.stock.cashflow.service.IndexService;
import com.stock.cashflow.utils.DateHelper;
import com.stock.cashflow.utils.ExcelHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Service
public class IndexServiceImpl implements IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);

    @Value("${foreign.api.host.baseurl}")
    private String foreignAPIHostAPIHost;

    @Value("${fireant.token}")
    private String fireantToken;

    @Value("${statistics.file.path}")
    private String statisticFile;

    @Value("${statistics.insert.new.row.index}")
    private int statisticsBeginRowIndex;

    Environment env;
    private final RestTemplate restTemplate;
    private final StockPriceRepository stockPriceRepository;
    private final ForeignTradingRepository foreignTradingRepository;
    private final IndexStatisticRepository indexStatisticRepository;
    private final ProprietaryTradingRepository proprietaryTradingRepository;
    private final ExcelHelper excelHelper;

    @Autowired
    public IndexServiceImpl(RestTemplate restTemplate, Environment env, StockPriceRepository stockPriceRepository,
                            ForeignTradingRepository foreignTradingRepository, IndexStatisticRepository indexStatisticRepository,
                            ExcelHelper excelHelper, ProprietaryTradingRepository proprietaryTradingRepository){
        this.restTemplate = restTemplate;
        this.env = env;
        this.stockPriceRepository = stockPriceRepository;
        this.foreignTradingRepository = foreignTradingRepository;
        this.indexStatisticRepository = indexStatisticRepository;
        this.excelHelper = excelHelper;
        this.proprietaryTradingRepository = proprietaryTradingRepository;
    }

    @Transactional
    @Override
    public void processIndexHistoricalQuotes(String index, String startDate, String endDate, IndexDTO dto) {
        Symbol[] data = null;
        try{
            String url = foreignAPIHostAPIHost + "symbols/" + index + "/historical-quotes?startDate=" + startDate + "&endDate=" + endDate + "&offset=0&limit=100";
            HttpHeaders headers = new HttpHeaders();
            headers.set(StockConstant.AUTHORIZATION, fireantToken);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            ResponseEntity<Symbol[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Symbol[].class);
            data = response.getBody();
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu fireant");
            throw ex;
        }

        if(!Objects.isNull(data)){
            try{
                Arrays.stream(data).forEach(sym -> {
                    log.info("Luu thong tin giao dich khoi ngoai cua ma {} cho ngay {}", sym.getSymbol(), sym.getDate());
                    StockPriceEntity entity = new StockPriceEntity();
                    entity.setSymbol(sym.getSymbol());
                    entity.setOpenPrice(sym.getPriceOpen());
                    entity.setClosePrice(sym.getPriceClose());
                    entity.setLowestPrice(sym.getPriceLow());
                    entity.setHighestPrice(sym.getPriceHigh());
                    entity.setTotalVolume(sym.getTotalVolume());
                    entity.setPercentageChange(dto.getPercentageChange());
                    entity.setPriceChange(dto.getPriceChange());

                    double priceRange = (sym.getPriceHigh() - sym.getPriceLow()) / sym.getPriceHigh() * 100;
                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);
                    entity.setPriceRange(df.format(priceRange) + "%");

                    Instant instant = sym.getDate().toInstant();
                    LocalDate tradingDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                    entity.setTradingDate(tradingDate);
                    entity.setHashDate(DigestUtils.sha256Hex(tradingDate + sym.getSymbol()));

                    stockPriceRepository.save(entity);

                    ForeignTradingEntity foreignTradingEntity = new ForeignTradingEntity();
                    foreignTradingEntity.setSymbol(sym.getSymbol());
                    foreignTradingEntity.setBuyValue(sym.getBuyForeignValue());
                    foreignTradingEntity.setBuyVolume(sym.getBuyForeignQuantity());
                    foreignTradingEntity.setSellVolume(sym.getSellForeignQuantity());
                    foreignTradingEntity.setSellValue(sym.getSellForeignValue());
                    foreignTradingEntity.setTradingDate(tradingDate);
                    foreignTradingEntity.setHashDate(DigestUtils.sha256Hex(tradingDate + sym.getSymbol()));
                    foreignTradingRepository.save(foreignTradingEntity);

                    log.info("Luu du lieu cua {} cho ngay {} thanh cong", entity.getSymbol(), entity.getTradingDate());
                });
            }
            catch(DataIntegrityViolationException ex){
                log.error("Du lieu da ton tai ", ex);
                log.error(ex.getMessage());
                throw ex;
            }
            catch(Exception ex){
                log.error("Loi trong qua trinh luu du lieu cho index");
                log.info(ex.getMessage());
                throw ex;
            }
        }else{
            log.error("Khong tim thay thong tin giao dich");
        }
    }

    @Transactional
    @Override
    public void analyzeIndex(String startDate, String endDate) {

        ArrayList<String> tradingDates = DateHelper.daysInRange(LocalDate.parse(startDate), LocalDate.parse(endDate));

        for (String date : tradingDates) {
            log.info("Phan tich index ngay {}", date);
            LocalDate tradingDate = LocalDate.parse(date);
            String vnindexHashDate = DigestUtils.sha256Hex(date + StockConstant.VNINDEX);
            String vn30HashDate = DigestUtils.sha256Hex(date + StockConstant.VN30);

            Double vnindexTotalVolume = stockPriceRepository.findTotalVolumeBySymbolAndHashDate(StockConstant.VNINDEX, vnindexHashDate);
            Double vn30TotalVolume = stockPriceRepository.findTotalVolumeBySymbolAndHashDate(StockConstant.VN30, vn30HashDate);

            try{
                saveIndexAnalyze(tradingDate, StockConstant.VN30,  vn30TotalVolume, vnindexTotalVolume);
                log.info("Saved VN30 ");
            } catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich VN30/VNINDEX");
            }

            try{
                String[] bluechip = StockGroupsConstant.BLUECHIP;
                long bluechipTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(bluechip), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.BLUE_CHIP,  bluechipTotalVolume, vnindexTotalVolume);
                log.info("Saved BLUE_CHIP ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich BLUECHIP/VNINDEX");
            }

            try{
                String[] midcap = StockGroupsConstant.MIDCAP_NO_BANK;
                long bluechipTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(midcap), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.MID_CAP,  bluechipTotalVolume, vnindexTotalVolume);
                log.info("Saved MID_CAP ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich MIDCAP/VNINDEX");
            }

            try{
                String[] smallcap = StockGroupsConstant.SMALLCAP;
                long bluechipTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(smallcap), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.SMALL_CAP,  bluechipTotalVolume, vnindexTotalVolume);
                log.info("Saved SMALL_CAP ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich SMALLCAP/VNINDEX");
            }

            try{
                String[] banks = IndustryConstant.BANKS;
                long bankTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(banks), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.BANKS,  bankTotalVolume, vnindexTotalVolume);
                log.info("Saved BANKS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich BANKS/VNINDEX");
            }

            try{
                String[] stocks = IndustryConstant.STOCKS;
                long stockTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(stocks), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.STOCKS,  stockTotalVolume, vnindexTotalVolume);
                log.info("Saved STOCKS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich STOCKS/VNINDEX");
            }

            try{
                String[] bds = IndustryConstant.BDS;
                long bdsTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(bds), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.BDS,  bdsTotalVolume, vnindexTotalVolume);
                log.info("Saved BDS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich BDS/VNINDEX");
            }

            try{
                String[] bdsKcn = IndustryConstant.BDS_KCN;
                long bdskcnTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(bdsKcn), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.BDS_KCN,  bdskcnTotalVolume, vnindexTotalVolume);
                log.info("Saved BDS_KCN ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich BDS_KCN/VNINDEX");
            }

            try{
                String[] retail = IndustryConstant.RETAIL;
                long retailTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(retail), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.RETAIL,  retailTotalVolume, vnindexTotalVolume);
                log.info("Saved RETAIL ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich RETAIL/VNINDEX");
            }

            try{
                String[] logistics = IndustryConstant.LOGISTICS;
                long logisticsTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(logistics), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.LOGISTICS,  logisticsTotalVolume, vnindexTotalVolume);
                log.info("Saved LOGISTICS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich LOGISTICS/VNINDEX");
            }

            try{
                String[] textile = IndustryConstant.TEXTILE;
                long textileTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(textile), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.TEXTILE,  textileTotalVolume, vnindexTotalVolume);
                log.info("Saved TEXTILE ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich TEXTILE/VNINDEX");
            }

            try{
                String[] wood = IndustryConstant.WOOD;
                long woodTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(wood), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.WOOD,  woodTotalVolume, vnindexTotalVolume);
                log.info("Saved WOOD ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich WOOD/VNINDEX");
            }

            try{
                String[] oilGas = IndustryConstant.OIL_GAS;
                long oilGasTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(oilGas), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.OIL_GAS,  oilGasTotalVolume, vnindexTotalVolume);
                log.info("Saved OIL_GAS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich OIL_GAS/VNINDEX");
            }

            try{
                String[] seafood = IndustryConstant.SEAFOOD;
                long seafoodTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(seafood), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.SEAFOOD,  seafoodTotalVolume, vnindexTotalVolume);
                log.info("Saved SEAFOOD ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich SEAFOOD/VNINDEX");
            }

            try{
                String[] metarials = IndustryConstant.METARIALS;
                long metarialsTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(metarials), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.METARIAL,  metarialsTotalVolume, vnindexTotalVolume);
                log.info("Saved METARIALS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich METARIALS/VNINDEX");
            }

            try{
                String[] construction = IndustryConstant.CONSTRUCTION;
                long constructionTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(construction), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.CONSTRUCTION,  constructionTotalVolume, vnindexTotalVolume);
                log.info("Saved CONSTRUCTION ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich CONSTRUCTION/VNINDEX");
            }

            try{
                String[] steels = IndustryConstant.STEELS;
                long steelsTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(steels), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.STEEL,  steelsTotalVolume, vnindexTotalVolume);
                log.info("Saved STEELS ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich STEELS/VNINDEX");
            }

            try{
                String[] electric = IndustryConstant.ELECTRIC;
                long electricTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(electric), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.ELECTRIC,  electricTotalVolume, vnindexTotalVolume);
                log.info("Saved ELECTRIC ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich ELECTRIC/VNINDEX");
            }

            try{
                String[] chemistryFertilizer = IndustryConstant.CHEMISTRY_FERTILIZER_PLASTIC;
                long chemistryFertilizerTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(chemistryFertilizer), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.CHEMISTRY_FERTILIZER,  chemistryFertilizerTotalVolume, vnindexTotalVolume);
                log.info("Saved CHEMISTRY_FERTILIZER ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich CHEMISTRY_FERTILIZER/VNINDEX");
            }

            try{
                String[] animals = IndustryConstant.ANIMALS;
                long animalsTotalVolume = stockPriceRepository.getTotalVolumeSum(List.of(animals), tradingDate);
                saveIndexAnalyze(tradingDate, StockConstant.ANIMALS,  animalsTotalVolume, vnindexTotalVolume);
                log.info("Saved CHEMISTRY_FERTILIZER ");
            }catch (Exception ex){
                ex.printStackTrace();
                log.error(ex.getMessage());
                throw new RuntimeException("Loi trong qua trinh phan tich CHEMISTRY_FERTILIZER/VNINDEX");
            }

        }
    }

    @Override
    public void analyzeIntraday(String date, IntradayDTO dto) {
        LocalDate tradingDate = LocalDate.parse(date);

        ZipSecureFile.setMinInflateRatio(0);
        try (FileInputStream fileInputStream = new FileInputStream(statisticFile); Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheet("Intraday");
            excelHelper.insertNewRow(sheet, statisticsBeginRowIndex);
            Row row = sheet.getRow(statisticsBeginRowIndex);

            Double foreignTNV = foreignTradingRepository.getForeignTotalNetValue(tradingDate);
            Integer foreignNumberOfBuy = foreignTradingRepository.getNumberOfBuy(tradingDate);
            Integer foreignNumberOfNoChange = foreignTradingRepository.getNumberOfNoChange(tradingDate);
            Integer foreignNumberOfSell = foreignTradingRepository.getNumberOfSell(tradingDate);

            Double proprietaryTNV = proprietaryTradingRepository.getForeignTotalNetValue(tradingDate);
            Integer proprietaryNumberOfBuy = proprietaryTradingRepository.getNumberOfBuy(tradingDate);
            Integer proprietaryNumberOfNoChange = proprietaryTradingRepository.getNumberOfNoChange(tradingDate);
            Integer proprietaryNumberOfSell = proprietaryTradingRepository.getNumberOfSell(tradingDate);

            excelHelper.updateCellDate(workbook, row, 1, date);
            // vnindex
            excelHelper.updateCellDouble(workbook, row, 2, Double.valueOf(dto.getVnindexPercentageChange())/100, true);
            excelHelper.updateCellLong(workbook, row, 4, dto.getVnindexUp());
            excelHelper.updateCellLong(workbook, row, 5, dto.getVnindexNoChange());
            excelHelper.updateCellLong(workbook, row, 6, dto.getVnindexDown());

            // vn30
            excelHelper.updateCellDouble(workbook, row, 7, Double.valueOf(dto.getVn30PercentageChange())/100, true);
            excelHelper.updateCellLong(workbook, row, 9, dto.getVn30Up());
            excelHelper.updateCellLong(workbook, row, 10, dto.getVn30NoChange());
            excelHelper.updateCellLong(workbook, row, 11, dto.getVn30Down());

            // foreign
            excelHelper.updateCellLong(workbook, row, 12, foreignNumberOfBuy);
            excelHelper.updateCellLong(workbook, row, 13, foreignNumberOfNoChange);
            excelHelper.updateCellLong(workbook, row, 14, foreignNumberOfSell);
            excelHelper.updateCellDouble(workbook, row, 15, foreignTNV, false);

            // proprieraty
            excelHelper.updateCellLong(workbook, row, 17, proprietaryNumberOfBuy);
            excelHelper.updateCellLong(workbook, row, 18, proprietaryNumberOfNoChange);
            excelHelper.updateCellLong(workbook, row, 19, proprietaryNumberOfSell);
            excelHelper.updateCellDouble(workbook, row, 20, proprietaryTNV, false);

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

    private void saveIndexAnalyze(LocalDate tradingDate, String symbol, double groupVolume, double totalVolume){
        String hashDate = DigestUtils.sha256Hex(tradingDate + symbol);
        IndexStatisticEntity entity = new IndexStatisticEntity();
        entity.setSymbol(symbol);
        entity.setVolume(groupVolume);
        entity.setTotalVolume(totalVolume);
        double percentageTaken = groupVolume / totalVolume * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        entity.setPercentageTakenOnIndex(df.format(percentageTaken) + "%");
        entity.setTradingDate(tradingDate);
        entity.setHashDate(hashDate);
        indexStatisticRepository.save(entity);
    }

}
