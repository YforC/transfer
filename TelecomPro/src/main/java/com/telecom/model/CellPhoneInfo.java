package com.telecom.model;

import java.math.BigDecimal;

public class CellPhoneInfo {
    private String cellPhoneNumber;
    private BigDecimal remainMoney;
    private String orderDesc;
    private Integer status;
    private String targetCarrier;

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public BigDecimal getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(BigDecimal remainMoney) {
        this.remainMoney = remainMoney;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTargetCarrier() {
        return targetCarrier;
    }

    public void setTargetCarrier(String targetCarrier) {
        this.targetCarrier = targetCarrier;
    }
}
