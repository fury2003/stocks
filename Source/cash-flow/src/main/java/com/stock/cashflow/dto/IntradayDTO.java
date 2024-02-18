package com.stock.cashflow.dto;

import lombok.Data;


@Data
public class IntradayDTO {

    private String vnindexPercentageChange;
    private Integer vnindexUp;
    private Integer vnindexNoChange;
    private Integer vnindexDown;

    private String vn30PercentageChange;
    private Integer vn30Up;
    private Integer vn30NoChange;
    private Integer vn30Down;

}
