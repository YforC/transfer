package com.unicom.service;

import com.unicom.dto.TransferRequest;
import com.unicom.dto.TransferResponse;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(name = "TransferNumberService", targetNamespace = "http://service.unicom.com/")
public interface TransferNumberService {

    @WebMethod(operationName = "transferNumber")
    @WebResult(name = "transferResponse", targetNamespace = "http://service.unicom.com/")
    TransferResponse transferNumber(
            @WebParam(name = "transferRequest", targetNamespace = "http://service.unicom.com/")
            TransferRequest request);
}
