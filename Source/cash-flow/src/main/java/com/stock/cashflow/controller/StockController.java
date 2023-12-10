package com.stock.cashflow.controller;

import com.stock.cashflow.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/symbols")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}/stock-price")
    public String getStockHistoricalData(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);

        stockService.processStockPrice(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan " + symbol + " thanh cong";
    }

    @GetMapping("/{symbol}/historical-quotes")
    public String getStockHistoricalData(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate ,@RequestHeader(value = "Authorization") String header) {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);

        stockService.processForeign(symbol, startDate, endDate, header);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan " + symbol + " thanh cong";
    }

    @GetMapping("/{symbol}/stock-intraday-latest")
    public String getStockIntraday(@PathVariable String symbol, @RequestParam String date) {
        log.info("Bat dau cap nhat so lenh mua ban cho ma chung khoan: {}", symbol);

        stockService.processIntraday(symbol, date);

        log.info("Ket thuc cap nhat so lenh mua ban cho ma chung khoan: {}", symbol);
        return "Cap nhat so lenh mua ban cho ma chung khoan " + symbol + " thanh cong";
    }

}
