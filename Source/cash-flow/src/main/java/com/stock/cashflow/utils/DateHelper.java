package com.stock.cashflow.utils;

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
}
