package com.stock.cashflow.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.stock.cashflow.persistence.entity.ForeignTradingEntity;
import com.stock.cashflow.persistence.entity.StockPriceEntity;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.decimal4j.util.DoubleRounder;

import java.math.RoundingMode;
import java.text.DecimalFormat;
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

        public ForeignTradingEntity convertToForeignEntity(){
                ForeignTradingEntity entity = new ForeignTradingEntity();
                entity.setSymbol(this.getSymbol());
                entity.setBuyValue(this.getBuyForeignValue());
                entity.setBuyVolume(this.getBuyForeignQuantity());
                entity.setSellValue(this.getSellForeignValue());
                entity.setSellVolume(this.getSellForeignQuantity());

                Instant instant = this.getDate().toInstant();
                LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();

                entity.setTradingDate(today);
                String hashDate = today.toString() + this.getSymbol();
                entity.setHashDate(DigestUtils.sha256Hex(hashDate));

                return entity;
        }

        public StockPriceEntity convertToStockPriceEntity(double yesterdayClosedPrice){
                StockPriceEntity entity = new StockPriceEntity();
                entity.setSymbol(this.getSymbol());
                entity.setHighestPrice(this.getPriceHigh()*1000);
                entity.setLowestPrice(this.getPriceLow()*1000);
                entity.setOpenPrice(this.getPriceOpen()*1000);
                entity.setClosePrice(this.getPriceClose()*1000);
                entity.setTotalVolume(this.getTotalVolume());


                double percentChange = this.getPriceClose() > yesterdayClosedPrice
                        ? (this.getPriceClose() - yesterdayClosedPrice) / yesterdayClosedPrice * 100
                        : -((yesterdayClosedPrice - this.getPriceClose()) / this.getPriceClose()) * 100;

                double priceRange = ((this.getPriceHigh() - this.getPriceLow()) / this.getPriceHigh()) * 100;
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.CEILING);

                entity.setPercentageChange(df.format(percentChange) + "%");
                entity.setPriceRange(df.format(priceRange) + "%");
                entity.setPriceChange(DoubleRounder.round(this.getPriceClose() - yesterdayClosedPrice, 2));

                Instant instant = this.getDate().toInstant();
                LocalDate today = instant.atZone(ZoneId.systemDefault()).toLocalDate();

                entity.setTradingDate(today);
                String hashDate = today.toString() + this.getSymbol();
                entity.setHashDate(DigestUtils.sha256Hex(hashDate));

                return entity;
        }
}
