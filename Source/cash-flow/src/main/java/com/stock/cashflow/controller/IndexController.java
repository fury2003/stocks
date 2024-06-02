package com.stock.cashflow.controller;

import com.stock.cashflow.dto.IndexDTO;
import com.stock.cashflow.dto.IntradayDTO;
import com.stock.cashflow.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/index")
public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }


    @PostMapping("/{index}/historical-quotes")
    public ResponseEntity<String> getIndexHistoricalQuotes(@PathVariable String index, @RequestParam String startDate, @RequestParam String endDate, @RequestBody IndexDTO dto) {
        log.info("Bat dau cap nhat du lieu index");

        indexService.processIndexHistoricalQuotes(index, startDate, endDate, dto);

        log.info("Ket thuc cap nhat du lieu index");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/analyze-index")
    public ResponseEntity<String> analyzeIndex(@RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau phan tich index");

        indexService.analyzeIndex(startDate, endDate);

        log.info("Ket thuc phan tich index");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/intraday-analysis")
    public ResponseEntity<String> intradayAnalysis(@RequestParam String tradingDate, @RequestBody IntradayDTO dto) {
        log.info("Bat dau phan tich intraday data");

        indexService.analyzeIntraday(tradingDate, dto);

        log.info("Ket thuc phan tich  intraday data");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trading-date")
    public ResponseEntity<String> addTradingDate(@RequestParam String tradingDate) {

        indexService.addTradingDate(tradingDate);
        return ResponseEntity.noContent().build();
    }

}
