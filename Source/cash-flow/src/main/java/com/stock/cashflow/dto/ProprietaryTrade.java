package com.stock.cashflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.ProprietaryTradingEntity;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Data
public class ProprietaryTrade {
    private String organCode;
    private String ticker;
    private String timeRange;
    private String marketType;
    private double priceChange;
    private double percentPriceChange;
    private double totalBuyTradeVolume;
    private double totalBuyTradeValue;
    private double totalSellTradeVolume;
    private double totalSellTradeValue;
    private double totalNetBuyTradeValue;
    private double totalNetSellTradeValue;
    private double totalNetBuyTradeVolume;
    private double totalNetSellTradeVolume;
    private double ceilingPrice;
    private double floorPrice;
    private double matchPrice;
    private double referencePrice;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date toDate;

    public ProprietaryTradingEntity convertToEntity(){
        ProprietaryTradingEntity entity = new ProprietaryTradingEntity();
        entity.setSymbol(this.getOrganCode());
        entity.setBuyValue(this.getTotalBuyTradeValue());
        entity.setBuyVolume(this.getTotalBuyTradeVolume());
        entity.setSellValue(this.getTotalSellTradeValue());
        entity.setSellVolume(this.getTotalSellTradeVolume());

        Instant instant = this.getToDate().toInstant();
        LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
//                String today = lastDateLocalDate.toString();
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate localDate = LocalDate.parse(new Date().toString(), formatter);

        entity.setTradingDate(today);
        String hashDate = today.toString() + this.getOrganCode();
        entity.setHashDate(DigestUtils.sha256Hex(hashDate));

        return entity;
    }
}
