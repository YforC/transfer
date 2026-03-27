package com.unicom.service;

import com.unicom.client.telecom.TransferRequest;
import com.unicom.client.telecom.TransferResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public class TelecomTransferGatewayService {
    private final String serviceAddress;

    public TelecomTransferGatewayService(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public TransferResponse submit(String phoneNumber) {
        TransferRequest request = new TransferRequest();
        request.setCellPhoneNumber(phoneNumber);

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
