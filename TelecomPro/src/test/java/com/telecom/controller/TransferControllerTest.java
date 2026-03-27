package com.telecom.controller;

import com.telecom.client.TransferResponse;
import com.telecom.model.CellPhoneInfo;
import com.telecom.service.PhoneLookupGatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhoneLookupGatewayService phoneLookupGatewayService;

    @Test
    void shouldCallUnicomGatewayWhenTargetCarrierIsUnicom() throws Exception {
        when(phoneLookupGatewayService.submitToUnicom(any(CellPhoneInfo.class))).thenReturn(successResponse());

        mockMvc.perform(post("/transfer/transferNumber")
                        .param("cellPhoneNumber", "13311111111")
                        .param("targetCarrier", "UNICOM"))
                .andExpect(status().isOk());

        verify(phoneLookupGatewayService).submitToUnicom(any(CellPhoneInfo.class));
    }

    @Test
    void shouldCallMobileGatewayWhenTargetCarrierIsMobile() throws Exception {
        when(phoneLookupGatewayService.submitToMobile(any(CellPhoneInfo.class))).thenReturn(successResponse());

        mockMvc.perform(post("/transfer/transferNumber")
                        .param("cellPhoneNumber", "13311111111")
                        .param("targetCarrier", "MOBILE"))
                .andExpect(status().isOk());

        verify(phoneLookupGatewayService).submitToMobile(any(CellPhoneInfo.class));
    }

    private TransferResponse successResponse() {
        TransferResponse response = new TransferResponse();
        response.setSuccess(true);
        response.setMessage("ok");
        response.setNextStep("next");
        return response;
    }
}
