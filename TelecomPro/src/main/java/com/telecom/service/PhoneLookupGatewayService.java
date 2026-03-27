package com.telecom.service;

import com.telecom.client.TransferRequest;
import com.telecom.client.TransferResponse;
import com.telecom.model.CellPhoneInfo;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PhoneLookupGatewayService {
    private final String unicomServiceAddress;
    private final String telecomServiceAddress;

    public PhoneLookupGatewayService(
            @Value("${transfer.service.address}") String unicomServiceAddress,
            @Value("${telecom.service.address}") String telecomServiceAddress) {
        this.unicomServiceAddress = unicomServiceAddress;
        this.telecomServiceAddress = telecomServiceAddress;
    }

    public TransferResponse submitToUnicom(CellPhoneInfo form) {
        validate(form);

        TransferRequest request = new TransferRequest();
        request.setCellPhoneNumber(form.getCellPhoneNumber().trim());

        try {
            JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
            factoryBean.setServiceClass(com.telecom.client.TransferNumberService.class);
            factoryBean.setAddress(unicomServiceAddress);
            com.telecom.client.TransferNumberService client =
                    (com.telecom.client.TransferNumberService) factoryBean.create();
            return client.transferNumber(request);
        } catch (Exception ex) {
            return failure("联通服务暂不可用。", "请确认服务地址可访问后重试。");
        }
    }

    public TransferResponse submitToMobile(CellPhoneInfo form) {
        validate(form);

        com.telecom.ws.TransferRequest request = new com.telecom.ws.TransferRequest();
        request.setCellPhoneNumber(form.getCellPhoneNumber().trim());

        try {
            JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
            factoryBean.setServiceClass(com.telecom.service.TransferNumberService.class);
            factoryBean.setAddress(telecomServiceAddress);
            com.telecom.service.TransferNumberService client =
                    (com.telecom.service.TransferNumberService) factoryBean.create();
            com.telecom.ws.TransferResponse wsResponse = client.transferNumber(request);
            TransferResponse response = new TransferResponse();
            response.setSuccess(wsResponse.isSuccess());
            response.setMessage(wsResponse.getMessage());
            response.setNextStep(wsResponse.getNextStep());
            return response;
        } catch (Exception ex) {
            return failure("移动服务暂不可用。", "请确认服务地址可访问后重试。");
        }
    }

    private void validate(CellPhoneInfo form) {
        if (form == null) {
            throw new IllegalArgumentException("请输入手机号。");
        }
        if (form.getCellPhoneNumber() == null || !form.getCellPhoneNumber().trim().matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的 11 位手机号。");
        }
    }

    private TransferResponse failure(String message, String nextStep) {
        TransferResponse response = new TransferResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setNextStep(nextStep);
        return response;
    }
}
