package com.telecom.ws;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "transferRequest", namespace = "http://service.telecom.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"cellPhoneNumber", "remainMoney", "orderDesc", "status"})
public class TransferRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cellPhoneNumber;
    private BigDecimal remainMoney;
    private String orderDesc;
    private Integer status;

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
}
