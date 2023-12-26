package com.stock.cashflow.controller;

import com.stock.cashflow.dto.IndexDTO;
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


    @PostMapping("/{index}/historical-quotes")
    public String getIndexHistoricalQuotes(@PathVariable String index, @RequestParam String startDate, @RequestParam String endDate, @RequestBody IndexDTO dto) {
        log.info("Bat dau cap nhat du lieu index");

        indexService.processIndexHistoricalQuotes(index, startDate, endDate, dto);

        log.info("Ket thuc cap nhat du lieu index");
        return "Cap nhat du lieu index thanh cong";
    }

}
