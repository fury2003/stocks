package com.stock.cashflow.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Symbol {

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private Date date;
        private String symbol;
        private double priceHigh;
        private double priceLow;
        private double priceOpen;
        private double priceAverage;
        private double priceClose;
        private double priceBasic;
        private double totalVolume;
        private double dealVolume;
        private double putthroughVolume;
        private double totalValue;
        private double putthroughValue;
        private double buyForeignQuantity;
        private double buyForeignValue;
        private double sellForeignQuantity;
        private double sellForeignValue;
        private double buyCount;
        private double buyQuantity;
        private double sellCount;
        private double sellQuantity;
        private double adjRatio;
        private double currentForeignRoom;
        private double propTradingNetDealValue;
        private double propTradingNetPTValue;
        private double propTradingNetValue;

        public ForeignTradingEntity convertToEntity(){
                ForeignTradingEntity entity = new ForeignTradingEntity();
                entity.setSymbol(this.getSymbol());
                entity.setBuyValue(this.getBuyForeignValue());
                entity.setBuyVolume(this.getBuyForeignQuantity());
                entity.setSellValue(this.getSellForeignValue());
                entity.setSellVolume(this.getSellForeignQuantity());

                Instant instant = this.getDate().toInstant();
                LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();
//                String today = lastDateLocalDate.toString();
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate localDate = LocalDate.parse(new Date().toString(), formatter);

                entity.setTradingDate(today);
                String hashDate = today.toString() + this.getSymbol();
                entity.setHashDate(DigestUtils.sha256Hex(hashDate));

                return entity;
        }
}
