package com.stock.cashflow.controller;

import com.stock.cashflow.service.ForeignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foreign")
public class ForeignController {

    private static final Logger log = LoggerFactory.getLogger(ForeignController.class);

    private final ForeignService foreignService;

    public ForeignController(ForeignService foreignService) {
        this.foreignService = foreignService;
    }

    @GetMapping("{symbol}/historical-quotes")
    public ResponseEntity<String> getDerivativesForeignData(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau cap nhat du lieu giao dich cua nuoc ngoai cho ma {} tu ngay {} den ngay {}", symbol, startDate, endDate);

        foreignService.process(symbol, startDate, endDate);

        log.info("Ket thuc cap nhat du lieu giao dich cua nuoc ngoai cho ma {} tu ngay {} den ngay {}", symbol, startDate, endDate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("all/historical-quotes")
    public ResponseEntity<String> getDerivativesForeignData(@RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau cap nhat du lieu giao dich cua nuoc ngoai cho tat ca ma chung khoan tu ngay {} den ngay {}", startDate, endDate);

        foreignService.processAll(startDate, endDate);

        log.info("Ket thuc cap nhat du lieu giao dich cua nuoc ngoai cho ma tu ngay {} den ngay {}", startDate, endDate);
        return ResponseEntity.noContent().build();
    }

}
