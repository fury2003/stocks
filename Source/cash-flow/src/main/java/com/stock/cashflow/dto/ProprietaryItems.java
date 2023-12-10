package com.stock.cashflow.dto;

import lombok.Data;

@Data
public class ProprietaryItems {

    private String comGroupCode;
    private ProprietaryToday today;
    private ProprietaryOneWeek oneWeek;
    private ProprietaryOneMonth oneMonth;
    private ProprietaryYearToDate yearToDate;

}
