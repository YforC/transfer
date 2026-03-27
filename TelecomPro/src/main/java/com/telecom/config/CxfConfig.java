package com.telecom.config;

import com.telecom.service.TransferNumberService;
import javax.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfConfig {
    @Bean
    public Endpoint telecomTransferEndpoint(Bus bus, TransferNumberService transferNumberService) {
        EndpointImpl endpoint = new EndpointImpl(bus, transferNumberService);
        endpoint.publish("/TransferSupportService");
        return endpoint;
    }
}
