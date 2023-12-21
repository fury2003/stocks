package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.StockPrice;
import com.stock.cashflow.dto.StockPriceDataResponse;
import com.stock.cashflow.persistence.entity.StockPriceEntity;
import com.stock.cashflow.persistence.repository.StockPriceRepository;
import com.stock.cashflow.service.StockPriceService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class StockPriceServiceImpl implements StockPriceService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceServiceImpl.class);

    @Value("${stockPrice.api.host.baseurl}")
    private String stockPriceAPIHost;

    private final RestTemplate restTemplate;

    private final StockPriceRepository stockPriceRepository;

    public StockPriceServiceImpl(RestTemplate restTemplate, StockPriceRepository stockPriceRepository){
        this.restTemplate = restTemplate;
        this.stockPriceRepository = stockPriceRepository;
    }

    @Override
    public void process(String symbol, String startDate, String endDate) {
        StockPriceDataResponse latestPriceDataResponse;
        try{
            latestPriceDataResponse = getStockPriceDataResponse(symbol, startDate, endDate);
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu tu SSI");
            log.info(ex.getMessage());
            throw ex;
        }
        if(!Objects.isNull(latestPriceDataResponse.getData())){
            List<StockPrice> prices = latestPriceDataResponse.getData();
            try{
                saveStockPrice(prices);
            }catch (Exception ex){
                log.error("Loi trong qua trinh them du lieu");
                log.info(ex.getMessage());
                throw ex;
            }
        }else
            log.error("Khong tim thay du lieu thay doi gia cho ma {}", symbol);
    }

    @Override
    public void processAll(String startDate, String endDate) {
        String[] symbols = SymbolConstant.SYMBOLS;
        for (int i = 0; i < symbols.length; i++) {
            StockPriceDataResponse latestPriceDataResponse;
            try{
                latestPriceDataResponse = getStockPriceDataResponse(symbols[i], startDate, endDate);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu tu SSI");
                log.info(ex.getMessage());
                throw ex;
            }

            if(!Objects.isNull(latestPriceDataResponse.getData())){
                List<StockPrice> prices = latestPriceDataResponse.getData();
                try{
                    saveStockPrice(prices);
                }catch (Exception ex){
                    log.error("Loi trong qua trinh them du lieu");
                    log.info(ex.getMessage());
                    throw ex;
                }
            }else
                log.error("Khong tim thay du lieu thay doi gia cho ma {}", symbols[i]);

        }
    }

    private void saveStockPrice(List<StockPrice> prices){
        prices.forEach(price -> {
            StockPriceEntity entity = new StockPriceEntity();
            entity.setSymbol(price.getSymbol());
            entity.setCeilingPrice(Double.parseDouble(price.getCeilingPrice()));
            entity.setFloorPrice(Double.parseDouble(price.getFloorPrice()));
            entity.setOpenPrice(Double.parseDouble(price.getOpenPrice()));
            entity.setClosePrice(Double.parseDouble(price.getClosePrice()));
            entity.setPriceChange(Double.parseDouble(price.getPriceChange()));
            entity.setTotalVolume(Double.parseDouble(price.getTotalMatchVol()));

            double percentageChange = Double.parseDouble(price.getPerPriceChange()) * 100;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            entity.setPercentageChange(df.format(percentageChange) + "%");


            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date formatStart = null;
            try {
                formatStart = inputFormat.parse(price.getTradingDate());
            } catch (ParseException e) {
                log.error("Loi trong qua trinh parse trading date");
                throw new RuntimeException(e);
            }
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            String stringDate = outputFormat.format(formatStart);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate today = LocalDate.parse(stringDate, formatter);

//            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//            LocalDate today = LocalDate.parse(dateTime.toString(), outputFormatter);

            entity.setTradingDate(today);
            entity.setHashDate(DigestUtils.sha256Hex(today + price.getSymbol()));

            stockPriceRepository.save(entity);
            log.info("Luu du lieu thay doi gia cho ma {} thanh cong", price.getSymbol());
        });
    }


    private StockPriceDataResponse getStockPriceDataResponse(String symbol, String startDate, String endDate) {

        String url = stockPriceAPIHost + symbol + "&fromDate=" + startDate + "&toDate=" + endDate;
        ResponseEntity<StockPriceDataResponse> response = restTemplate.exchange(url, HttpMethod.GET, null, StockPriceDataResponse.class);
        return response.getBody();

    }
}
