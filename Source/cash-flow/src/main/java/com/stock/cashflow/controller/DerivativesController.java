package com.stock.cashflow.controller;

import com.stock.cashflow.dto.DerivativesDTO;
import com.stock.cashflow.dto.DerivativesProprietaryDTO;
import com.stock.cashflow.service.DerivativesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/derivatives")
public class DerivativesController {

    private static final Logger log = LoggerFactory.getLogger(DerivativesController.class);

    private final DerivativesService derivativesService;

    public DerivativesController(DerivativesService derivativesService) {
        this.derivativesService = derivativesService;
    }


    @PostMapping("/market/historical-quotes")
    public String getMarketDerivativesData(@RequestBody List<DerivativesDTO> dto) {
        log.info("Bat dau cap nhat du lieu phai sinh");

        derivativesService.processMarketData(dto);

        log.info("Ket thuc cap nhat du lieu phai sinh");
        return "Ket thuc cap nhat du lieu phai sinh";
    }

    @PostMapping("/foreign/{symbol}")
    public String getForeignDerivativesData(@PathVariable String symbol, @RequestParam String startDate, @RequestParam String endDate) {
        log.info("Bat dau cap nhat du lieu phai sinh cua khoi ngoai");

        derivativesService.processForeignData(symbol, startDate, endDate);

        log.info("Ket thuc cap nhat du lieu phai sinh cua khoi ngoai");
        return "Ket thuc cap nhat du lieu phai sinh cua khoi ngoai";
    }

    @PostMapping(value = "{symbol}/proprietary", produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateProprietaryData(@PathVariable String symbol, @RequestParam String tradingDate, @RequestBody DerivativesProprietaryDTO derivativesProprietaryDTO) {
        log.info("Bat dau cap nhat du lieu phai sinh cua tu doanh cho ngay {}", tradingDate);

        derivativesService.updateProprietary(symbol, tradingDate, derivativesProprietaryDTO);

        log.info("Ket thuc cap nhat du lieu phai sinh cua tu doanh cho ngay {}", tradingDate);

        return "Ket thuc cap nhat du lieu phai sinh cua tu doanh vao ngay " + tradingDate;
    }

}
