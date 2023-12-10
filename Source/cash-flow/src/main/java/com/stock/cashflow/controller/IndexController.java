package com.stock.cashflow.controller;

import com.stock.cashflow.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/index")
public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping("/{index}/historical-quotes")
    public String getIndexHistoricalData(@PathVariable String index, @RequestParam String date, @RequestHeader(value = "Authorization") String header) {
        log.info("Bat dau cap nhat du lieu cho {}", index);

        indexService.process(index, date, header);

        log.info("Ket thuc cap nhat du lieu cho {} ", index);
        return "Cap nhat du lieu cho index thanh cong";
    }

    @GetMapping("/{index}/proprietary-trading-value")
    public String getProprietaryTradingValue(@PathVariable String index) {
        log.info("Bat dau cap nhat du lieu tu doanh");

        indexService.processProprietaryTradingValue(index, "");

        log.info("Ket thuc cap nhat du lieu tu doanh");
        return "Cap nhat du lieu tu doanh thanh cong";
    }

}
