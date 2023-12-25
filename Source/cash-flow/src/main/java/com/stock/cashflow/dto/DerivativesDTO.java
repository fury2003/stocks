package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class DerivativesDTO {

    private String tradingDate;
    private String stockCode;
    private int lastVol;
    private Double change;
    private Double perChange;
    private Double totalVol;
    private Double totalVal;
    private int totalOIVol;
    private int foreignBuyVol;
    private Double foreignBuyVal;
    private int foreignSellVol;
    private Double foreignSellVal;

}
