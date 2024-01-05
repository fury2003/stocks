package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.dto.IndexDTO;
import com.stock.cashflow.dto.Symbol;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.StockPriceEntity;
import com.stock.cashflow.persistence.repository.ForeignTradingRepository;
import com.stock.cashflow.persistence.repository.StockPriceRepository;
import com.stock.cashflow.service.IndexService;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
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

    @Autowired
    public IndexServiceImpl(RestTemplate restTemplate, Environment env, StockPriceRepository stockPriceRepository, ForeignTradingRepository foreignTradingRepository){
        this.restTemplate = restTemplate;
        this.env = env;
        this.stockPriceRepository = stockPriceRepository;
        this.foreignTradingRepository = foreignTradingRepository;
    }


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

}
