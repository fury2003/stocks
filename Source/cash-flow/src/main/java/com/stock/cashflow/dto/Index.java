package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Index {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date date;
    private String symbol;
    private double priceHigh;
    private double priceLow;
    private double priceOpen;
    private double priceAverage;
    private double priceClose;
    private double priceBasic;
    private double totalVolume;
    private double dealVolume;
    private double putthroughVolume;
    private double totalValue;
    private double putthroughValue;
    private double buyForeignQuantity;
    private double buyForeignValue;
    private double sellForeignQuantity;
    private double sellForeignValue;
    private double buyCount;
    private double buyQuantity;
    private double sellCount;
    private double sellQuantity;
    private double adjRatio;
    private double currentForeignRoom;
    private double propTradingNetDealValue;
    private double propTradingNetPTValue;
    private double propTradingNetValue;
}
