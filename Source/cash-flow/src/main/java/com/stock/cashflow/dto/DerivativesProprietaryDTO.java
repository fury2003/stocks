package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class DerivativesProprietaryDTO {

    @JsonProperty("proprietaryBuyVol")
    private Double proprietaryBuyVol;

    @JsonProperty("proprietarySellVol")
    private Double proprietarySellVol;

    @JsonProperty("proprietaryBuyValue")
    private Double proprietaryBuyValue;

    @JsonProperty("proprietarySellValue")
    private Double proprietarySellValue;

    @JsonProperty("openInterest")
    private Long openInterest;
}
