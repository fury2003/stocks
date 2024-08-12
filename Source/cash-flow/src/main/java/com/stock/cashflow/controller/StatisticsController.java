package com.stock.cashflow.controller;

import com.stock.cashflow.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

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

    @PostMapping("/all/update-data")
    public ResponseEntity<String> updateStatisticForSpecificColumn(@RequestParam String tradingDate, @RequestParam String column) {
        log.info("Bat dau cap nhat du lieu cho {}", column);

        statisticsService.writeSpecificDataAllSymbolSpecificDate(tradingDate, column);

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

    @PostMapping("/all/highlight-orderbook")
    public ResponseEntity<String> highlightOrderBook(@RequestParam String tradingDate) {
        log.info("Bat dau ghi du lieu cho tat ca ma chung khoan");

        statisticsService.highlightOrderBook(tradingDate);

        log.info("Ket thuc xu ly cho ma chung khoan");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/proprietary/top-buy-sell")
    public ResponseEntity<String> writeTopProprietaryBuySell(@RequestParam String tradingDate, @RequestParam boolean isLastDayOfWeek){
        log.info("Bat dau ghi du lieu thong khe mua ban");

        statisticsService.writeProprietaryTopBuySell(tradingDate, isLastDayOfWeek);

        log.info("Ket thuc ghi du lieu thong khe mua ban");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/foreign/top-buy-sell")
    public ResponseEntity<String> writeTopForeignBuySell(@RequestParam String tradingDate, @RequestParam boolean isLastDayOfWeek){
        log.info("Bat dau ghi du lieu thong khe mua ban");

        statisticsService.writeForeignTopBuySell(tradingDate, isLastDayOfWeek);

        log.info("Ket thuc ghi du lieu thong khe mua ban");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/all/order-book/analyze")
    public ResponseEntity<String> analyzeOrderbook(@RequestParam String tradingDate){
        log.info("Bat dau ghi du lieu thong khe mua ban");

        statisticsService.analyzeOrderBook(tradingDate);

        log.info("Ket thuc ghi du lieu thong khe mua ban");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/intraday/rewrite-data")
    public ResponseEntity<String> updateIntradayData(@RequestParam String tradingDate, @RequestParam String updatedColumn) {
        log.info("Bat dau ghi du lieu thong khe mua ban");

        statisticsService.updateIntradayData(tradingDate, updatedColumn);

        log.info("Ket thuc ghi du lieu thong khe mua ban");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/moneyflow/rewrite-data")
    public ResponseEntity<String> updateMoneyFlowData(@RequestParam String tradingDate, @RequestParam String updatedColumn) {
        log.info("Bat dau ghi du lieu thong khe mua ban");

        statisticsService.updateMoneyFlowData(tradingDate, updatedColumn);

        log.info("Ket thuc ghi du lieu thong khe mua ban");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{symbol}/order-book/date-to-date")
    public ResponseEntity<String> updateOrderBookDateToDate(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau xu ly cho ma chung khoan: {}", symbol);

        statisticsService.writeOrderBookFromDateToDate(symbol, startDate, endDate);

        log.info("Ket thuc xu ly cho ma chung khoan: {}", symbol);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/vn30/rewrite-data")
    public ResponseEntity<String> updateVn30BuySellDateToDate(@RequestParam String startDate, @RequestParam String endDate, @RequestParam String updatedColumn) throws FileNotFoundException {
        log.info("Bat dau xu ly cho ma chung khoan");

        statisticsService.updateBuySellInVn30(startDate, endDate, updatedColumn);

        log.info("Ket thuc xu ly cho ma chung khoan");
        return ResponseEntity.noContent().build();
    }

}
