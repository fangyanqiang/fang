package com.fyq.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author fyq
 * @date 2020/7/7
 */
@Data
@Table(name = "stock")
public class Stock {
    private int id;

    private String stockCode;

    private String stockName;

    private BigDecimal stockPrice;

    private BigDecimal stockChange;

    private BigDecimal stockChangePrice;

    private BigDecimal stockChangeSpeed;

    private BigDecimal stockHandoverScale;

    private BigDecimal stockLiangBi;

    private BigDecimal stockAmplitude;

    private BigDecimal stockDealAmount;

    private BigDecimal stockFlowStockNumber;

    private BigDecimal stockFlowMarketValue;

    private BigDecimal stockMarketTtm;
}
