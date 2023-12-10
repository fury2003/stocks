package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Intraday {
    private String code;
    private String floor;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date tradingDate;

    private String time;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tradingDateTime;

    private double open;
    private double high;
    private double low;
    private double last;
    private double adLast;
    private double accumulatedVal;
    private double accumulatedVol;
    private double lastVol;
    private String side;
}
