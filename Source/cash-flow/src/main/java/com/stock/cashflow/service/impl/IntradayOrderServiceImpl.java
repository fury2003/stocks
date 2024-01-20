package com.stock.cashflow.service.impl;

import com.stock.cashflow.constants.StockConstant;
import com.stock.cashflow.constants.SymbolConstant;
import com.stock.cashflow.dto.Intraday;
import com.stock.cashflow.dto.IntradayData;
import com.stock.cashflow.persistence.entity.IntradayOrderEntity;
import com.stock.cashflow.persistence.entity.OrderBookEntity;
import com.stock.cashflow.persistence.repository.IntradayOrderRepository;
import com.stock.cashflow.persistence.repository.OrderBookRepository;
import com.stock.cashflow.service.IntradayOrderService;
import com.stock.cashflow.utils.FileHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class IntradayOrderServiceImpl implements IntradayOrderService {

    private static final Logger log = LoggerFactory.getLogger(IntradayOrderServiceImpl.class);

    @Value("${intraday.api.host.baseurl}")
    private String intradayAPI;

    @Value("${order.report.host.baseurl}")
    private String orderReportAPI;

    @Value("${order.report.dir}")
    private String reportFolder;

    @Value("${order.report.volume.column.index}")
    private int volumeColumnIdx;

    private final RestTemplate restTemplate;

    private final IntradayOrderRepository intradayOrderRepository;

    private final OrderBookRepository orderBookRepository;

    public IntradayOrderServiceImpl(RestTemplate restTemplate, IntradayOrderRepository intradayOrderRepository, OrderBookRepository orderBookRepository){
        this.restTemplate = restTemplate;
        this.intradayOrderRepository = intradayOrderRepository;
        this.orderBookRepository = orderBookRepository;
    }


    @Override
    public void process(String symbol) {
        IntradayData intradayData = null;
        try{
            intradayData = getIntradayDataResponse(symbol);
        }catch (Exception ex){
            log.error("Loi trong qua trinh truy xuat du lieu");
            throw ex;
        }

        if(!Objects.isNull(intradayData.getData())){
            try{
                saveIntradayOrderData(symbol, intradayData.getData());
            }catch (Exception ex){
                log.error("Loi trong qua trinh them du lieu");
                log.info(ex.getMessage());
                throw ex;
            }

        }else
            log.error("Khong tim thay so lenh giao dich cho ma {}", symbol);

    }

    @Override
    public void processAll() {
        String[] symbols = SymbolConstant.SYMBOLS;

        for (int i = 0; i < symbols.length; i++) {
            IntradayData intradayData = null;
            try{
                intradayData = getIntradayDataResponse(symbols[i]);
            }catch (Exception ex){
                log.error("Loi trong qua trinh truy xuat du lieu");
                throw ex;
            }

            if(!Objects.isNull(intradayData.getData())){
                try{
                    saveIntradayOrderData(symbols[i], intradayData.getData());
                }catch (Exception ex){
                    log.error("Loi trong qua trinh them du lieu");
                    log.info(ex.getMessage());
                    throw ex;
                }
            }else
                log.error("Khong tim thay so lenh giao dich cho ma {}", symbols[i]);
        }

    }

    @Override
    public void downloadOrderReport(String symbol) {

        Instant instant = new Date().toInstant();
        LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        String parentFolder = reportFolder + today;
        Path path = Paths.get(parentFolder);

        try {
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                Files.createDirectories(path);
                log.info("Tao folder moi cho ngay {}", today);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception according to your requirements
            log.info("Loi khi tao folder report cho ngay {}", today);
        }

        log.info("Tai report cho ma {}", symbol);
        String url = String.format(orderReportAPI, symbol);
        String destinationPath = parentFolder + "/" + symbol + ".xlsx";

        AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();

        asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(Response response) throws Exception {
                if (response.getStatusCode() == 200) {
                    try (ReadableByteChannel readableByteChannel = Channels.newChannel(response.getResponseBodyAsStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(destinationPath);
                         FileChannel fileChannel = fileOutputStream.getChannel()) {

                        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        log.info("Download file {} thanh cong", destinationPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    log.error("Failed to download file. Status code: " + response.getStatusCode());
                }

                // Close the AsyncHttpClient
                asyncHttpClient.close();

                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                try {
                    asyncHttpClient.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        if (FileHelper.fileExists(destinationPath)) {
            log.info("File downloaded successfully!");
        }
    }

    @Transactional
    @Override
    public void analyzeOrder(String tradingDate) {
        String sourceFolder = reportFolder + tradingDate;
        List<String> allFiles = FileHelper.getAllXLSXFiles(sourceFolder);

        for (String filePath : allFiles) {
            log.info("Xu ly file: " + filePath);
            String symbol = filePath.substring(filePath.length() - 16, filePath.length() - 13);
//            String symbol = filePath.substring(filePath.length() - 8, filePath.length() - 5);
            try {
                HashMap<String, Integer> report = processExcelFile(filePath, volumeColumnIdx + 1, volumeColumnIdx, volumeColumnIdx + 4);
                OrderBookEntity entity = new OrderBookEntity();
                entity.setSymbol(symbol);
                entity.setBuyOrder(report.get(StockConstant.BUY_ORDER));
                entity.setSellOrder(report.get(StockConstant.SELL_ORDER));
                entity.setBuyVolume(report.get(StockConstant.BUY_VOLUME));
                entity.setSellVolume(report.get(StockConstant.SELL_VOLUME));
                entity.setBigBuyOrder(report.get(StockConstant.BIG_BUY_ORDER));
                entity.setBigSellOrder(report.get(StockConstant.BIG_SELL_ORDER));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(tradingDate, formatter);
                entity.setTradingDate(date);
                entity.setHashDate(DigestUtils.sha256Hex(tradingDate + symbol));

                orderBookRepository.save(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static HashMap<String, Integer> processExcelFile(String filePath, int priceColumnIdx, int volColumnIdx, int sideColumnIdx) throws IOException {
        int buyOrder = 0;
        int buyVol = 0;
        int sellOrder = 0;
        int sellVol = 0;

        int bigBuyOrder = 0;
        int bigSellOrder = 0;

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("Sheet1");

            for (Row row : sheet) {
                Cell orderCell = row.getCell(sideColumnIdx - 1);
                if (orderCell != null) {
                    if(orderCell.getStringCellValue().equals("M")){
                        buyOrder += 1;
                        Cell volCell = row.getCell(volColumnIdx - 1);
                        buyVol += volCell.getNumericCellValue();

                        Cell priceCell = row.getCell(priceColumnIdx - 1);
                        String priceString = priceCell.getStringCellValue();
                        String price = priceString.substring(0, priceString.indexOf(".")).replace(",", ".") ;
                        double value = Double.parseDouble(price) * volCell.getNumericCellValue();
                        log.info("Khoi luong giao dich: {}", volCell.getNumericCellValue());
                        log.info("Gia tri giao dich: {}", value);
                        if( value >= 100000)
                            bigBuyOrder += 1;

                    } if(orderCell.getStringCellValue().equals("B")){
                        sellOrder += 1;
                        Cell volCell = row.getCell(volColumnIdx - 1);
                        sellVol += volCell.getNumericCellValue();

                        Cell priceCell = row.getCell(priceColumnIdx - 1);
                        String priceString = priceCell.getStringCellValue();
                        String price = priceString.substring(0, priceString.indexOf(".")).replace(",", ".") ;
                        double value = Double.parseDouble(price) * volCell.getNumericCellValue();
                        log.info("Khoi luong giao dich: {}", volCell.getNumericCellValue());
                        log.info("Gia tri giao dich: {}", value);
                        if( value >= 100000)
                            bigSellOrder += 1;
                    }
                }
            }
        }

        HashMap<String, Integer> report = new HashMap<>();
        report.put(StockConstant.BUY_ORDER, buyOrder);
        report.put(StockConstant.SELL_ORDER, sellOrder);
        report.put(StockConstant.BUY_VOLUME, buyVol);
        report.put(StockConstant.SELL_VOLUME, sellVol);
        report.put(StockConstant.BIG_BUY_ORDER, bigBuyOrder);
        report.put(StockConstant.BIG_SELL_ORDER, bigSellOrder);

        return report;
    }

    private void saveIntradayOrderData(String symbol, List<Intraday> intradayData){
        int buyOrder = 0;
        int sellOrder = 0;
        int buyVolume = 0;
        int sellVolume = 0;

        String floor = intradayData.get(0).getFloor();
        String tradingDate = String.valueOf(intradayData.get(0).getTradingDate());
        if(floor.equals("UPCOM") || floor.equals("HNX")){
            log.info("Khong tim thay thong ke giao dich cho ma chung khoan tren san UPCOM va HNX");
            return;
        }

        for (Intraday item : intradayData) {
            String side = item.getSide();
            if(Objects.isNull(side))
                continue;
            if(side.equals("PS")){
                buyOrder++;
                buyVolume += item.getLastVol();
            }else if(side.equals("PB")){
                sellOrder++;
                sellVolume += item.getLastVol();
            }
        }
        log.info("Tong so lenh mua cua ma {}={}", symbol, buyOrder);
        log.info("Tong so lenh ban cua ma {}={}", symbol, sellOrder);
        log.info("Tong khoi luong mua cua ma {}={}", symbol, buyVolume);
        log.info("Tong khoi luong ban cua ma {}={}", symbol, sellVolume);

        IntradayOrderEntity entity = new IntradayOrderEntity();
        entity.setSymbol(symbol);
        entity.setBuyOrder(buyOrder);
        entity.setSellOrder(sellOrder);
        entity.setBuyVolume(buyVolume);
        entity.setSellVolume(sellVolume);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(tradingDate, formatter);
        entity.setTradingDate(date);
        entity.setHashDate(DigestUtils.sha256Hex(tradingDate + symbol));

        intradayOrderRepository.save(entity);
        log.info("Luu thong tin so lenh giao dich cua ma {} thanh cong", entity.getSymbol());

    }

    private IntradayData getIntradayDataResponse(String symbol){
        log.info("Truy xuat danh sach so lenh cho ma {}", symbol);
        String url = intradayAPI + symbol;
        ResponseEntity<IntradayData> response = restTemplate.exchange(url, HttpMethod.GET, null, IntradayData.class);
        return response.getBody();

    }
}
