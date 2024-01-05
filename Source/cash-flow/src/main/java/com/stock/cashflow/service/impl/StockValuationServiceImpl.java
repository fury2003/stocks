package com.stock.cashflow.service.impl;

import com.stock.cashflow.persistence.entity.BalanceSheetEntity;
import com.stock.cashflow.persistence.entity.CashFlowEntity;
import com.stock.cashflow.persistence.entity.IncomeSheetEntity;
import com.stock.cashflow.persistence.repository.BalanceSheetRepository;
import com.stock.cashflow.persistence.repository.CashFlowRepository;
import com.stock.cashflow.persistence.repository.IncomeSheetRepository;
import com.stock.cashflow.service.StockValuationService;
import com.stock.cashflow.utils.ExcelHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StockValuationServiceImpl implements StockValuationService {

    private static final Logger log = LoggerFactory.getLogger(StockValuationServiceImpl.class);

    private final IncomeSheetRepository incomeSheetRepository;
    private final BalanceSheetRepository balanceSheetRepository;
    private final CashFlowRepository cashFlowRepository;
    private final ExcelHelper excelHelper;

    public StockValuationServiceImpl(IncomeSheetRepository incomeSheetRepository, BalanceSheetRepository balanceSheetRepository, CashFlowRepository cashFlowRepository, ExcelHelper excelHelper){
        this.incomeSheetRepository = incomeSheetRepository;
        this.balanceSheetRepository = balanceSheetRepository;
        this.cashFlowRepository = cashFlowRepository;
        this.excelHelper = excelHelper;
    }

    @Override
    public void writeDataForSpecificQuarter(String ticker, String quarter) {
        String hashQuarter = DigestUtils.sha256Hex(quarter + ticker);
        IncomeSheetEntity incomeSheetEntity = incomeSheetRepository.findIncomeSheetEntitiesByHashQuarter(hashQuarter);
        BalanceSheetEntity balanceSheetEntity = balanceSheetRepository.findBalanceSheetEntitiesByHashQuarter(hashQuarter);
        CashFlowEntity cashFlowEntity = cashFlowRepository.findCashFlowEntitiesByHashQuarter(hashQuarter);
        if(!Objects.isNull(incomeSheetEntity) && !Objects.isNull(balanceSheetEntity) && !Objects.isNull(cashFlowEntity))
            excelHelper.writeQuarterFinancialStatementToFile(ticker, balanceSheetEntity, incomeSheetEntity, cashFlowEntity);
        else{
            throw new RuntimeException("Ko tim thay bao cao tai chinh cua cong ty " + ticker);
        }

        log.info("Ghi du lieu bao cao tai chinh cho {} cua cong ty {} thanh cong", quarter, ticker);
    }

    @Override
    public void writeDataFromQuarterTo(String ticker, String fromQuarter, String toQuarter) {
        List<IncomeSheetEntity> incomeSheetEntities = incomeSheetRepository.findIncomeSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
        List<BalanceSheetEntity> balanceSheetEntities = balanceSheetRepository.findBalanceSheetEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);
        List<CashFlowEntity> cashFlowEntities = cashFlowRepository.findCashFlowEntitiesByTickerAndQuarterBetweenOrderByIdDesc(ticker, fromQuarter, toQuarter);

        if(!incomeSheetEntities.isEmpty() && !balanceSheetEntities.isEmpty() && !cashFlowEntities.isEmpty())
            excelHelper.writeMultipleQuarterFinancialStatementToFile(ticker, balanceSheetEntities, incomeSheetEntities, cashFlowEntities);
        else
            log.warn("Ko tim thay bao cao tai chinh cua cong ty {}", ticker);
        log.info("Ghi du lieu bao cao tai chinh tu {} den {} cua cong ty {} thanh cong", fromQuarter, toQuarter, ticker);

    }

    @Override
    public void writeDataForSpecificYear(String ticker, String quarter) {

    }
}
