package com.stock.cashflow.controller;

import com.stock.cashflow.service.IndexService;
import com.stock.cashflow.service.ProprietaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proprietary")
public class ProprietaryController {

    private static final Logger log = LoggerFactory.getLogger(ProprietaryController.class);

    private final ProprietaryService proprietaryService;

    public ProprietaryController(ProprietaryService proprietaryService) {
        this.proprietaryService = proprietaryService;
    }

    @GetMapping("/{floor}/historical-quotes")
    public String getProprietaryTradingValue(@PathVariable String floor) {
        log.info("Bat dau cap nhat du lieu tu doanh tren san {}", floor);

        proprietaryService.process(floor);

        log.info("Bat dau cap nhat du lieu tu doanh tren san {}", floor);
        return "Cap nhat du lieu tu doanh thanh cong";
    }

    @GetMapping("/all/historical-quotes")
    public String getProprietaryTradingValue() {
        log.info("Bat dau cap nhat du lieu tu doanh tren tat ca san");

        proprietaryService.processAll();

        log.info("Bat dau cap nhat du lieu tu doanh tren tat ca san");
        return "Cap nhat du lieu tu doanh thanh cong";
    }

}
