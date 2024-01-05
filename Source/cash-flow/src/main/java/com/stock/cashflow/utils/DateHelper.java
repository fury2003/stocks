package com.stock.cashflow.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateHelper {

    public static String determineQuarter(int month) {
        if (month >= 1 && month <= 3) {
            return "Q1";
        } else if (month >= 4 && month <= 6) {
            return "Q2";
        } else if (month >= 7 && month <= 9) {
            return "Q3";
        } else {
            return "Q4";
        }
    }

    public static String parseDateFormat(String tradingDate){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(tradingDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String output = date.format(outputFormatter);

        return output;
    }
}
