package com.stock.cashflow.controller;

import com.stock.cashflow.service.StockValuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public String writeFStoWrite(@PathVariable String ticker, @RequestParam String quarter) {
        log.info("Bat dau thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);

        stockValuationService.writeDataForSpecificQuarter(ticker, quarter);

        log.info("Ket thuc thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);
        return "Hoan thanh thuc hien ghi du lieu bao cao tai chinh vao file";
    }

    @PostMapping("/company/{ticker}/from-to")
    public String writeFStoWriteFromTo(@PathVariable String ticker, @RequestParam String fromQuarter, @RequestParam String toQuarter) {
        log.info("Bat dau thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);

        stockValuationService.writeDataFromQuarterTo(ticker, fromQuarter, toQuarter);

        log.info("Ket thuc thuc hien ghi du lieu bao cao tai chinh cua cong ty {} vao file", ticker);
        return "Hoan thanh thuc hien ghi du lieu bao cao tai chinh vao file";
    }
}
