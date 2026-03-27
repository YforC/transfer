package com.telecom.service;

import com.telecom.ws.TransferRequest;
import com.telecom.ws.TransferResponse;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = "http://service.telecom.com/")
public interface TransferNumberService {
    @WebMethod(operationName = "transferNumber")
    @WebResult(name = "transferResponse", targetNamespace = "http://service.telecom.com/")
    TransferResponse transferNumber(
            @WebParam(name = "transferRequest", targetNamespace = "http://service.telecom.com/")
            TransferRequest request);
}
