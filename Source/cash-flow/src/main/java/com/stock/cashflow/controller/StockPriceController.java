package com.stock.cashflow.controller;

import com.stock.cashflow.service.StockPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/price")
public class StockPriceController {

    private static final Logger log = LoggerFactory.getLogger(StockPriceController.class);

    private final StockPriceService stockPriceService;


    public StockPriceController(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }


    @GetMapping("/{symbol}/stock-price")
    public ResponseEntity<String> getStockPrice(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);
        stockPriceService.process(symbol, startDate, endDate);
        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/stock-price/ssi")
    public ResponseEntity<String> getStockPriceAll(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        log.info("Bat dau xu ly du lieu thay doi gia cho tat ca ma chung khoan");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date formatStart = inputFormat.parse(startDate);
        Date formatEnd = inputFormat.parse(endDate);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        String start = outputFormat.format(formatStart);
        String end = outputFormat.format(formatEnd);

        stockPriceService.processAllSSI(start, end);

        log.info("Ket thuc xu ly du lieu thay doi gia  cho tat ca ma chung khoan");
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/all/stock-price/fireant")
    public ResponseEntity<String> getStockPriceAllFireant(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        log.info("Bat dau lay du lieu thay doi gia cho tat ca ma chung khoan tu fireant");

        stockPriceService.processAllFireant(startDate, endDate);

        log.info("Ket thuc lay du lieu thay doi gia cho tat ca ma chung khoan tu fireant");
        return ResponseEntity.noContent().build();
    }

    private String convertDate(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date formatStart = inputFormat.parse(date);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        return outputFormat.format(formatStart);
    }
}
