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
    private int proprietaryBuyVol;

    @JsonProperty("proprietarySellVol")
    private int proprietarySellVol;

    @JsonProperty("proprietaryNetVol")
    private int proprietaryNetVol;

    @JsonProperty("proprietaryBuyValue")
    private Double proprietaryBuyValue;

    @JsonProperty("proprietarySellValue")
    private Double proprietarySellValue;

    @JsonProperty("proprietaryNetValue")
    private Double proprietaryNetValue;

}
