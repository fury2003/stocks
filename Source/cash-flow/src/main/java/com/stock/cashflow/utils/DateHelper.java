package com.stock.cashflow.utils;

import org.springframework.cglib.core.Local;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class DateHelper {

    public static String parseDateFormat(String tradingDate){
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(tradingDate, inputFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String output = date.format(outputFormatter);

        return output;
    }

    public static ArrayList<String> daysInRange(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;

        ArrayList<String> daysBetween = new ArrayList<>();
        endDate = endDate.plusDays(1);
        while (!currentDate.isEqual(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if(dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY){
                currentDate = currentDate.plusDays(1);
                continue;
            }
            daysBetween.add(currentDate.toString());
            currentDate = currentDate.plusDays(1);
        }
        return daysBetween;
    }

    public static LocalDate getCurrentLocalDate(){
        Instant instant = new Date().toInstant();
        LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return today;
    }

}
