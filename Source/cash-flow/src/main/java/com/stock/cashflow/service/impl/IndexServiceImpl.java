package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.StockGroupsConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.IndexDTO;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.IndexStatisticEntity;
import com.stock.cashflow.persistence.entity.StockPriceEntity;
import com.stock.cashflow.persistence.repository.ForeignTradingRepository;
import com.stock.cashflow.persistence.repository.IndexStatisticRepository;
import com.stock.cashflow.persistence.repository.StockPriceRepository;
import com.stock.cashflow.service.IndexService;
import com.stock.cashflow.utils.DateHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.DateUtil;
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

    Environment env;
    private final RestTemplate restTemplate;
    private final StockPriceRepository stockPriceRepository;
    private final ForeignTradingRepository foreignTradingRepository;
    private final IndexStatisticRepository indexStatisticRepository;

    @Autowired
    public IndexServiceImpl(RestTemplate restTemplate, Environment env, StockPriceRepository stockPriceRepository,
                            ForeignTradingRepository foreignTradingRepository, IndexStatisticRepository indexStatisticRepository){
        this.restTemplate = restTemplate;
        this.env = env;
        this.stockPriceRepository = stockPriceRepository;
        this.foreignTradingRepository = foreignTradingRepository;
        this.indexStatisticRepository = indexStatisticRepository;
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
