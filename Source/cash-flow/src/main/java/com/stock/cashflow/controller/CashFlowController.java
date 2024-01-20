package com.stock.cashflow.controller;

import com.stock.cashflow.service.CashFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cash-flow")
public class CashFlowController {

    private static final Logger log = LoggerFactory.getLogger(CashFlowController.class);

    private final CashFlowService cashFlowService;

    public CashFlowController(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    @GetMapping("/company/{ticker}")
    public ResponseEntity<String> getMarketDerivativesData(@PathVariable String ticker, @RequestParam String period, @RequestParam String size) {
        log.info("Bat dau trich xuat du lieu balance sheet");

        cashFlowService.crawlData(ticker, period, size);

        log.info("Ket thuc trich xuat du lieu balance sheet");
        return ResponseEntity.noContent().build();
    }

}
