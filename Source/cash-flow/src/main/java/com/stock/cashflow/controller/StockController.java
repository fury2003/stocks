package com.stock.cashflow.controller;

import com.stock.cashflow.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}/stock-writer")
    public String getStockStatistic(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly so lieu cho ma : {}", symbol);

        stockService.processStockPrice(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan " + symbol + " thanh cong";
    }


    @GetMapping("/all/stock-writer")
    public String getAllStocksStatistic(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);

        stockService.processStockPrice(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan " + symbol + " thanh cong";
    }

}
