package com.stock.cashflow.controller;

import com.stock.cashflow.service.StockValuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/financial-statement")
public class StockValuationController {
    private static final Logger log = LoggerFactory.getLogger(StockValuationController.class);

    private final StockValuationService stockValuationService;

    public StockValuationController(StockValuationService stockValuationService) {
        this.stockValuationService = stockValuationService;
    }

    @PostMapping("/company/{ticker}")
    public ResponseEntity<String> writeFStoWrite(@PathVariable String ticker, @RequestParam String quarter) {
        log.info("Bat dau thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);

        stockValuationService.writeDataForSpecificQuarter(ticker, quarter);

        log.info("Ket thuc thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/company/{ticker}/from-to")
    public ResponseEntity<String> writeFStoWriteFromTo(@PathVariable String ticker, @RequestParam String fromQuarter, @RequestParam String toQuarter) {
        log.info("Bat dau thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);

        stockValuationService.writeDataFromQuarterTo(ticker, fromQuarter, toQuarter);

        log.info("Ket thuc thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/company/{ticker}/update-data")
    public ResponseEntity<String> writeSpecificColumn(@PathVariable String ticker, @RequestParam String fromQuarter, @RequestParam String toQuarter, @RequestParam int column) {
        log.info("Bat dau thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);

        stockValuationService.writeDataFromToForSpecificColumn(ticker, fromQuarter, toQuarter, column);

        log.info("Ket thuc thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/company/latest-price")
    public ResponseEntity<String> updateLatestPrice(@RequestParam String tradingDate) {
        log.info("Bat dau cap nhat gia moi nhat");

        stockValuationService.updateLatestPrice(tradingDate);

        log.info("Ket thuc cap nhat gia moi nhat");
        return ResponseEntity.noContent().build();
    }

}
