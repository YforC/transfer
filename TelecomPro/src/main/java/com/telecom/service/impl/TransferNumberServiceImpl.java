package com.telecom.service.impl;

import com.telecom.model.CellPhoneInfo;
import com.telecom.repository.CellPhoneInfoRepository;
import com.telecom.service.TransferNumberService;
import com.telecom.ws.TransferRequest;
import com.telecom.ws.TransferResponse;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import javax.jws.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@WebService(
        serviceName = "TransferSupportService",
        endpointInterface = "com.telecom.service.TransferNumberService",
        targetNamespace = "http://service.telecom.com/")
public class TransferNumberServiceImpl implements TransferNumberService {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");

    private final CellPhoneInfoRepository sourceRepository;
    private final CellPhoneInfoRepository targetRepository;

    @Autowired
    public TransferNumberServiceImpl(
            @Qualifier("unicomCellPhoneInfoRepository") CellPhoneInfoRepository sourceRepository,
            @Qualifier("mobileCellPhoneInfoRepository") CellPhoneInfoRepository targetRepository) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
    }

    @Override
    public TransferResponse transferNumber(TransferRequest request) {
        if (request == null) {
            return fail("申请数据不能为空。", "请返回页面重新填写后提交。");
        }

        String mobile = trim(request.getCellPhoneNumber());
        if (mobile == null || !MOBILE_PATTERN.matcher(mobile).matches()) {
            return fail("手机号格式不正确。", "请输入 11 位手机号后重试。");
        }

        BigDecimal remainMoney = request.getRemainMoney();
        if (remainMoney == null || remainMoney.compareTo(BigDecimal.ZERO) < 0) {
            return fail("余额不能为空且不能为负数。", "请修正余额后重试。");
        }

        String orderDesc = trim(request.getOrderDesc());
        if (orderDesc == null || orderDesc.isEmpty()) {
            return fail("套餐说明不能为空。", "请填写套餐说明后重试。");
        }

        CellPhoneInfo source = sourceRepository.findByPhoneNumber(mobile);
        if (source == null) {
            return fail("源运营商中不存在该号码。", "请确认号码当前归属后重试。");
        }

        source.setStatus(1);
        sourceRepository.updateByPhoneNumber(source);

        CellPhoneInfo target = targetRepository.findByPhoneNumber(mobile);
        if (target == null) {
            CellPhoneInfo created = new CellPhoneInfo();
            created.setCellPhoneNumber(mobile);
            created.setRemainMoney(remainMoney);
            created.setOrderDesc(orderDesc);
            created.setStatus(0);
            targetRepository.insert(created);
        } else {
            target.setRemainMoney(remainMoney);
            target.setOrderDesc(orderDesc);
            target.setStatus(0);
            targetRepository.updateByPhoneNumber(target);
        }

        TransferResponse response = new TransferResponse();
        response.setSuccess(true);
        response.setMessage("号码 " + mobile + " 已成功转入移动。");
        response.setNextStep("源库状态已更新为转出，移动库状态已更新为当前运营商。");
        return response;
    }

    private TransferResponse fail(String message, String nextStep) {
        TransferResponse response = new TransferResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setNextStep(nextStep);
        return response;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
