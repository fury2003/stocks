package com.stock.cashflow.controller;

import com.stock.cashflow.service.IntradayOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/intraday")
public class IntradayOrderController {

    private static final Logger log = LoggerFactory.getLogger(IntradayOrderController.class);

    private final IntradayOrderService intradayOrderService;

    public IntradayOrderController(IntradayOrderService intradayOrderService) {
        this.intradayOrderService = intradayOrderService;
    }

    @GetMapping("/{symbol}/order-latest")
    public String getIntradayOrder(@PathVariable String symbol) {
        log.info("Bat dau cap nhat so lenh mua ban cho ma chung khoan: {}", symbol);

        intradayOrderService.process(symbol);

        log.info("Ket thuc cap nhat so lenh mua ban cho ma chung khoan: {}", symbol);
        return "Cap nhat so lenh mua ban cho ma chung khoan " + symbol + " thanh cong";
    }

    @GetMapping("/all/order-latest")
    public String getAllIntradayOrder() {
        log.info("Bat dau cap nhat so lenh mua ban cho tat ca");

        intradayOrderService.processAll();

        log.info("Ket thuc cap nhat so lenh mua ban cho tat ca");
        return "Ket thuc cap nhat so lenh mua ban cho tat ca";
    }

    @GetMapping("/{symbol}/download-order-report")
    public String downloadStatisticOrderFile(@PathVariable String symbol) {
        log.info("Bat dau tai file so lenh");

        intradayOrderService.downloadOrderReport(symbol);

        log.info("Ket thuc tai file so lenh");
        return "Tai file so lenh thanh cong";
    }

    @GetMapping("/all/analyze-orders")
    public String analyzeOrders(@RequestParam String tradingDate) {
        log.info("Bat dau phan tich so lenh");

        intradayOrderService.analyzeOrder(tradingDate);

        log.info("Ket thuc phan tich so lenh");
        return "Hoan thanh phan tich so lenh";
    }
}
