package com.stock.cashflow.controller;

import com.stock.cashflow.service.BalanceSheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/balance-sheet")
public class BalanceSheetController {

    private static final Logger log = LoggerFactory.getLogger(BalanceSheetController.class);

    private final BalanceSheetService balanceSheetService;

    public BalanceSheetController(BalanceSheetService balanceSheetService) {
        this.balanceSheetService = balanceSheetService;
    }

    @GetMapping("/company/{ticker}")
    public String getMarketDerivativesData(@PathVariable String ticker, @RequestParam String period, @RequestParam String size) {
        log.info("Bat dau trich xuat du lieu balance sheet");

        balanceSheetService.crawlData(ticker, period, size);

        log.info("Ket thuc trich xuat du lieu balance sheet");
        return "Ket thuc trich xuat du lieu balance sheet";
    }

}
