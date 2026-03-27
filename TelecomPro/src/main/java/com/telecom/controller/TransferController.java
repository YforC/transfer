package com.telecom.controller;

import com.telecom.client.TransferResponse;
import com.telecom.model.CellPhoneInfo;
import com.telecom.service.PhoneLookupGatewayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TransferController {
    private final PhoneLookupGatewayService phoneLookupGatewayService;
    private final String serviceAddress;

    public TransferController(
            PhoneLookupGatewayService phoneLookupGatewayService,
            @Value("${transfer.service.address}") String serviceAddress) {
        this.phoneLookupGatewayService = phoneLookupGatewayService;
        this.serviceAddress = serviceAddress;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        if (!model.containsAttribute("cellPhoneInfo")) {
            CellPhoneInfo cellPhoneInfo = new CellPhoneInfo();
            cellPhoneInfo.setTargetCarrier("UNICOM");
            model.addAttribute("cellPhoneInfo", cellPhoneInfo);
        }
        model.addAttribute("serviceAddress", serviceAddress);
        return "phone-transfer-index";
    }

    @PostMapping("/transfer/transferNumber")
    public String transferNumber(@ModelAttribute CellPhoneInfo cellPhoneInfo, Model model) {
        model.addAttribute("cellPhoneInfo", cellPhoneInfo);

        try {
            TransferResponse response = "MOBILE".equalsIgnoreCase(cellPhoneInfo.getTargetCarrier())
                    ? phoneLookupGatewayService.submitToMobile(cellPhoneInfo)
                    : phoneLookupGatewayService.submitToUnicom(cellPhoneInfo);
            model.addAttribute("success", response.isSuccess());
            model.addAttribute("info", response.getMessage());
            model.addAttribute("nextStep", response.getNextStep());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("success", false);
            model.addAttribute("info", ex.getMessage());
            model.addAttribute("nextStep", "检查手机号后重试。");
        }

        return "phone-transfer-result";
    }
}
