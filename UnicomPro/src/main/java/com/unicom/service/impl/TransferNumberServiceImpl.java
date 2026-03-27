package com.unicom.service.impl;

import com.unicom.dto.TransferRequest;
import com.unicom.dto.TransferResponse;
import com.unicom.entity.CellPhoneInfo;
import com.unicom.repository.CellPhoneInfoRepository;
import com.unicom.service.TransferNumberService;
import java.util.regex.Pattern;
import javax.jws.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@WebService(
        serviceName = "TransferSupportService",
        endpointInterface = "com.unicom.service.TransferNumberService",
        targetNamespace = "http://service.unicom.com/")
public class TransferNumberServiceImpl implements TransferNumberService {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");

    private final CellPhoneInfoRepository sourceRepository;
    private final CellPhoneInfoRepository targetRepository;

    @Autowired
    public TransferNumberServiceImpl(
            @Qualifier("mobileCellPhoneInfoRepository") CellPhoneInfoRepository sourceRepository,
            @Qualifier("unicomCellPhoneInfoRepository") CellPhoneInfoRepository targetRepository) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
    }

    @Override
    public TransferResponse transferNumber(TransferRequest request) {
        if (request == null) {
            return fail("申请数据不能为空。", "请重新提交。");
        }

        String phoneNumber = trim(request.getCellPhoneNumber());
        if (phoneNumber == null || !MOBILE_PATTERN.matcher(phoneNumber).matches()) {
            return fail("手机号格式不正确。", "请输入 11 位手机号。");
        }

        CellPhoneInfo source = sourceRepository.findByPhoneNumber(phoneNumber);
        if (source == null) {
            return fail("源运营商中不存在该号码。", "请确认号码归属后重试。");
        }

        source.setStatus(1);
        sourceRepository.updateByPhoneNumber(source);

        CellPhoneInfo target = targetRepository.findByPhoneNumber(phoneNumber);
        if (target == null) {
            CellPhoneInfo created = new CellPhoneInfo();
            created.setCellPhoneNumber(phoneNumber);
            created.setRemainMoney(source.getRemainMoney());
            created.setOrderDesc(source.getOrderDesc());
            created.setStatus(0);
            targetRepository.insert(created);
        } else {
            target.setRemainMoney(source.getRemainMoney());
            target.setOrderDesc(source.getOrderDesc());
            target.setStatus(0);
            targetRepository.updateByPhoneNumber(target);
        }

        TransferResponse response = new TransferResponse();
        response.setSuccess(true);
        response.setMessage("号码 " + phoneNumber + " 已成功转入联通。");
        response.setNextStep("号码资料已自动同步。");
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
