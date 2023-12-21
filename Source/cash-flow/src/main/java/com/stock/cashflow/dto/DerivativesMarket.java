package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DerivativesMarket {

    @JsonProperty("tradingTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDate tradingTime;

    @JsonProperty("change")
    private Double change;

    @JsonProperty("changePercent")
    private Double changePercent;

    @JsonProperty("open")
    private Double open;

    @JsonProperty("high")
    private Double high;

    @JsonProperty("low")
    private Double low;

    @JsonProperty("close")
    private Double close;

    @JsonProperty("avgPrice")
    private Double avgPrice;

    @JsonProperty("adjClose")
    private Double adjClose;

    @JsonProperty("totalVolume")
    private Double totalVolume;

    @JsonProperty("ptVol")
    private Double ptVol;

    @JsonProperty("totalDealVolume")
    private Double totalDealVolume;

    @JsonProperty("rS52W")
    private Double rS52W;

    @JsonProperty("rS1M")
    private Double rS1M;

    @JsonProperty("rS3M")
    private Double rS3M;

    @JsonProperty("rS6M")
    private Double rS6M;
}
