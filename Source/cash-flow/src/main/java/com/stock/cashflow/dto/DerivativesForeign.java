package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DerivativesForeign {

    @JsonProperty("tradingTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDate tradingTime;

    @JsonProperty("foreignRoom")
    private String foreignRoom;

    @JsonProperty("foreignBuyVol")
    private Double foreignBuyVol;

    @JsonProperty("foreignSellVol")
    private Double foreignSellVol;

    @JsonProperty("foreignBuyValue")
    private Double foreignBuyValue;

    @JsonProperty("foreignSellValue")
    private Double foreignSellValue;

}
