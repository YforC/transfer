package com.unicom.service;

import com.unicom.client.telecom.TransferRequest;
import com.unicom.client.telecom.TransferResponse;
import java.math.BigDecimal;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public class TelecomTransferGatewayService {
    private final String serviceAddress;

    public TelecomTransferGatewayService(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public TransferResponse submit(String phoneNumber, String remainMoney, String orderDesc, String statusText) {
        TransferRequest request = new TransferRequest();
        request.setCellPhoneNumber(phoneNumber);
        request.setRemainMoney(new BigDecimal(remainMoney));
        request.setOrderDesc(orderDesc);
        request.setStatus(Integer.valueOf(statusText));

        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(com.unicom.client.telecom.TransferNumberService.class);
        factoryBean.setAddress(serviceAddress);
        com.unicom.client.telecom.TransferNumberService client =
                (com.unicom.client.telecom.TransferNumberService) factoryBean.create();
        return client.transferNumber(request);
    }

    public String getServiceAddress() {
        return serviceAddress;
    }
}
