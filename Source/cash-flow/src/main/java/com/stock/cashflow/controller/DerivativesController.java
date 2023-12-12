package com.stock.cashflow.controller;

import com.stock.cashflow.service.DerivativesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/derivatives")
public class DerivativesController {

    private static final Logger log = LoggerFactory.getLogger(DerivativesController.class);

    private final DerivativesService derivativesService;

    public DerivativesController(DerivativesService derivativesService) {
        this.derivativesService = derivativesService;
    }


    @GetMapping("{symbol}/historical-quotes")
    public String getDerivativesForeignData(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate ,@RequestHeader(value = "Authorization") String header) {
        log.info("Bat dau cap nhat du lieu phai sinh cua nuoc ngoai tu ngay {} den ngay {}", startDate, endDate);

        derivativesService.process(symbol, startDate, endDate, header);

        log.info("Ket thuc cap nhat du lieu phai sinh cua nuoc ngoai tu ngay {} den ngay {}", startDate, endDate);
        return "Ket thuc cap nhat du lieu phai sinh cua nuoc ngoai tu ngay " + startDate + " den ngay " + endDate;
    }

}
