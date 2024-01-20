package com.stock.cashflow.controller;

import com.stock.cashflow.service.BalanceSheetService;
import com.stock.cashflow.service.IncomeSheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/income-sheet")
public class IncomeSheetController {

    private static final Logger log = LoggerFactory.getLogger(IncomeSheetController.class);

    private final IncomeSheetService incomeSheetService;

    public IncomeSheetController(IncomeSheetService incomeSheetService) {
        this.incomeSheetService = incomeSheetService;
    }

    @GetMapping("/company/{ticker}")
    public ResponseEntity<String> getMarketDerivativesData(@PathVariable String ticker, @RequestParam String period, @RequestParam String size) {
        log.info("Bat dau trich xuat du lieu income sheet");

        incomeSheetService.crawlData(ticker, period, size);

        log.info("Ket thuc trich xuat du lieu income sheet");
        return ResponseEntity.noContent().build();
    }

}
