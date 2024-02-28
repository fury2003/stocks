package com.stock.cashflow.controller;

import com.stock.cashflow.dto.ProprietaryDataResponse;
import com.stock.cashflow.service.ProprietaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proprietary")
public class ProprietaryController {

    private static final Logger log = LoggerFactory.getLogger(ProprietaryController.class);

    private final ProprietaryService proprietaryService;

    public ProprietaryController(ProprietaryService proprietaryService) {
        this.proprietaryService = proprietaryService;
    }

    @GetMapping("/all/historical-quotes-fireant")
    public ResponseEntity<String> getProprietaryTradingFromFireant() {
        log.info("Bat dau cap nhat du lieu tu doanh tu fireant");

        proprietaryService.processFireant();

        log.info("Bat dau cap nhat du lieu tu doanh tu fireant");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/historical-quotes-ssi")
    public ResponseEntity<String> getProprietaryTradingFromSSI() {
        log.info("Bat dau cap nhat du lieu tu doanh tu ssi");

        proprietaryService.processAllFloorsFromSSI();

        log.info("Bat dau cap nhat du lieu tu doanh tu ssi");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("volatile-trading")
    public ResponseEntity<String> getVolatileTrading(@RequestParam String tradingDate) {
        proprietaryService.processVolatileTrading(tradingDate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{floor}/historical-quotes-ssi")
    public ResponseEntity<String> getProprietaryTradingFromSSI(@PathVariable String floor, @RequestBody ProprietaryDataResponse proprietaryTextData) {
        log.info("Bat dau cap nhat du lieu tu doanh san {} tu SSI", floor);

        proprietaryService.processSSI(proprietaryTextData);

        log.info("Bat dau cap nhat du lieu tu doanh tu ssi");
        return ResponseEntity.noContent().build();
    }

}
