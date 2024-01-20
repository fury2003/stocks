package com.stock.cashflow.controller;

import com.stock.cashflow.service.IntradayOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> getIntradayOrder(@PathVariable String symbol) {
        log.info("Bat dau cap nhat so lenh mua ban cho ma chung khoan: {}", symbol);

        intradayOrderService.process(symbol);

        log.info("Ket thuc cap nhat so lenh mua ban cho ma chung khoan: {}", symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/order-latest")
    public ResponseEntity<String> getAllIntradayOrder() {
        log.info("Bat dau cap nhat so lenh mua ban cho tat ca");

        intradayOrderService.processAll();

        log.info("Ket thuc cap nhat so lenh mua ban cho tat ca");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{symbol}/download-order-report")
    public ResponseEntity<String> downloadStatisticOrderFile(@PathVariable String symbol) {
        log.info("Bat dau tai file so lenh");

        intradayOrderService.downloadOrderReport(symbol);

        log.info("Ket thuc tai file so lenh");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/analyze-orders")
    public ResponseEntity<String> analyzeOrders(@RequestParam String tradingDate) {
        log.info("Bat dau phan tich so lenh");

        intradayOrderService.analyzeOrder(tradingDate);

        log.info("Ket thuc phan tich so lenh");
        return ResponseEntity.noContent().build();
    }
}
