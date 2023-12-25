package com.stock.cashflow.controller;

import com.stock.cashflow.service.StatisticsService;
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
    public String updateStatisticSpecificDate(@PathVariable String symbol, @RequestParam String tradingDate) {
        log.info("Bat dau xu ly so lieu cho ma : {}", symbol);

        statisticsService.writeSpecificDate(symbol, tradingDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan " + symbol + " thanh cong";
    }


    @PostMapping("/{symbol}/stock/date-to-date")
    public String updateStatisticDateToDate(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);

        statisticsService.writeDateToDate(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return "Cap nhat du lieu cho ma chung khoan thanh cong";
    }

    @PostMapping("/all/stock/specific-date")
    public String updateStatisticForAll(@RequestParam String tradingDate) {
        log.info("Bat dau ghi du lieu cho tat ca ma chung khoan");

        statisticsService.writeAllForSpecificDate(tradingDate);

        log.info("Ket thuc xu ly cho ma chung khoan");
        return "Ghi du lieu cho tat ca ma chung khoan thanh cong";
    }

    @PostMapping("/{symbol}/derivatives/date-to-date")
    public String updateDerivativesStatisticDateToDate(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma phai sinh: {}", symbol);

        statisticsService.writeDerivativesDateToDate(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma phai sinh: {}", symbol);
        return "Cap nhat du lieu cho ma phai sinh thanh cong";
    }

}
