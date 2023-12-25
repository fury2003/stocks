package com.stock.cashflow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DerivativesForeignItem {

    private LocalDateTime tradingTime;
    private int foreignBuyVol;
    private int foreignSellVol;
    private Double foreignBuyValue;
    private Double foreignSellValue;

}
