package com.stock.cashflow.controller;

import com.stock.cashflow.service.IndexService;
import com.stock.cashflow.service.ProprietaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/all/historical-quotes-fireant")
    public ResponseEntity<String> getProprietaryTradingFromSSI() {
        log.info("Bat dau cap nhat du lieu tu doanh tu fireant");

        proprietaryService.processFireant();

        log.info("Bat dau cap nhat du lieu tu doanh tu fireant");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/historical-quotes-ssi")
    public ResponseEntity<String> getProprietaryTradingFromFireant() {
        log.info("Bat dau cap nhat du lieu tu doanh tu ssi");

        proprietaryService.processSSI();

        log.info("Bat dau cap nhat du lieu tu doanh tu ssi");
        return ResponseEntity.noContent().build();
    }

}
