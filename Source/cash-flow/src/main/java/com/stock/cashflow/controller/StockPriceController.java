package com.stock.cashflow.controller;

import com.stock.cashflow.service.StockPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public String getStockPrice(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);
        String start = convertDate(startDate);
        String end = convertDate(endDate);
        stockPriceService.process(symbol, start, end);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan " + symbol + " thanh cong";
    }

    @GetMapping("/all/stock-price")
    public String getStockPriceAll(@RequestParam String startDate, @RequestParam String endDate) throws ParseException {
        log.info("Bat dau xu ly du lieu thay doi gia cho tat ca ma chung khoan");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date formatStart = inputFormat.parse(startDate);
        Date formatEnd = inputFormat.parse(endDate);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        String start = outputFormat.format(formatStart);
        String end = outputFormat.format(formatEnd);

        stockPriceService.processAll(start, end);

        log.info("Ket thuc xu ly du lieu thay doi gia  cho tat ca ma chung khoan");
        return "Cap nhat du lieu thay doi gia cho tat ca ma chung khoan thanh cong";
    }

    private String convertDate(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date formatStart = inputFormat.parse(date);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        return outputFormat.format(formatStart);
    }
}
