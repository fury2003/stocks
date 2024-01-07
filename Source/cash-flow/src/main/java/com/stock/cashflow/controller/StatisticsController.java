package com.stock.cashflow.controller;

import com.stock.cashflow.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PostMapping("/{symbol}/stock/specific-date")
    public ResponseEntity<String> updateStatisticSpecificDate(@PathVariable String symbol, @RequestParam String tradingDate) {
        log.info("Bat dau xu ly so lieu cho ma : {}", symbol);

        statisticsService.writeSpecificDate(symbol, tradingDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{symbol}/stock/date-to-date")
    public ResponseEntity<String> updateStatisticDateToDate(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);

        statisticsService.writeDateToDate(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/all/stock/specific-date")
    public ResponseEntity<String> updateStatisticForAll(@RequestParam String tradingDate) {
        log.info("Bat dau ghi du lieu cho tat ca ma chung khoan");

        statisticsService.writeAllForSpecificDate(tradingDate);

        log.info("Ket thuc xu ly cho ma chung khoan");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{symbol}/derivatives/date-to-date")
    public ResponseEntity<String> updateDerivativesStatisticDateToDate(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma phai sinh: {}", symbol);

        statisticsService.writeDerivativesDateToDate(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma phai sinh: {}", symbol);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/all/update-data")
    public ResponseEntity<String> updateStatisticForSpecificColumn(@RequestParam String tradingDate, @RequestParam String column) {
        log.info("Bat dau cap nhat du lieu cho {}", column);

        statisticsService.writeSpecificDataAllSymbolSpecificDate(tradingDate, column);

        log.info("Ket thuc cap nhat du lieu cho {}", column);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{symbol}/update-data")
    public ResponseEntity<String> updateSpecificDataSpecificSymbolFromTo(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate, @RequestParam String column) {
        log.info("Bat dau cap nhat du lieu cho {}", column);

        statisticsService.writeSpecificDataSpecificSymbolFromTo(symbol, startDate, endDate, column);

        log.info("Ket thuc cap nhat du lieu cho {}", column);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/index/money-flow")
    public ResponseEntity<String> updateIndexAnalyzed(@RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau cap nhat du lieu phan tich index");

        statisticsService.writeIndexAnalyzedDateToDate(startDate, endDate);

        log.info("Ket thuc cap nhat du lieu phan tich index");
        return ResponseEntity.noContent().build();
    }
}
