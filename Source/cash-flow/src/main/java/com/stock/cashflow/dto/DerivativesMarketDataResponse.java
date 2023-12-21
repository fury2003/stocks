package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DerivativesMarketDataResponse {
    @JsonProperty("now")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX")
    private Date now;

    @JsonProperty("result")
    private DerivativesMarketItems result;

    @JsonProperty("targetUrl")
    private String targetUrl;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("error")
    private String error;

    @JsonProperty("unAuthorizedRequest")
    private boolean unAuthorizedRequest;

    @JsonProperty("__abp")
    private boolean abp;
}
