package com.telecom.service.impl;

import com.telecom.repository.CellPhoneInfoRepository;
import com.telecom.ws.TransferRequest;
import com.telecom.ws.TransferResponse;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferNumberServiceImplTest {
    private CellPhoneInfoRepository unicomRepository;
    private CellPhoneInfoRepository mobileRepository;

    @BeforeEach
    void setUp() {
        unicomRepository = new CellPhoneInfoRepository(createJdbcTemplate("telecom_unicom"));
        mobileRepository = new CellPhoneInfoRepository(createJdbcTemplate("telecom_mobile"));
    }

    @Test
    void shouldTransferFromUnicomToMobile() {
        mobileRepository.insert(record("13312345678", "0.00", "placeholder", 1));
        unicomRepository.insert(record("13312345678", "10.00", "old", 0));

        TransferNumberServiceImpl service = new TransferNumberServiceImpl(unicomRepository, mobileRepository);
        TransferResponse response = service.transferNumber(request("13312345678", "19.00", "mobile-plan"));

        assertTrue(response.isSuccess());
        assertEquals(1, unicomRepository.findByPhoneNumber("13312345678").getStatus());
        assertEquals(0, mobileRepository.findByPhoneNumber("13312345678").getStatus());
        assertEquals("mobile-plan", mobileRepository.findByPhoneNumber("13312345678").getOrderDesc());
    }

    @Test
    void shouldFailWhenSourceNumberMissing() {
        TransferNumberServiceImpl service = new TransferNumberServiceImpl(unicomRepository, mobileRepository);

        TransferResponse response = service.transferNumber(request("13399999999", "19.00", "mobile-plan"));

        assertFalse(response.isSuccess());
        assertEquals("源运营商中不存在该号码。", response.getMessage());
        assertNull(mobileRepository.findByPhoneNumber("13399999999"));
    }

    private JdbcTemplate createJdbcTemplate(String name) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(createDataSource(name));
        jdbcTemplate.execute("drop table if exists cell_phone_info");
        jdbcTemplate.execute("create table cell_phone_info ("
                + "id bigint auto_increment primary key, "
                + "cell_phone_number varchar(20), "
                + "remain_money decimal(10,2), "
                + "order_desc varchar(100), "
                + "status tinyint)");
        return jdbcTemplate;
    }

    private DataSource createDataSource(String name) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + name + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private com.telecom.model.CellPhoneInfo record(String phoneNumber, String money, String orderDesc, int status) {
        com.telecom.model.CellPhoneInfo info = new com.telecom.model.CellPhoneInfo();
        info.setCellPhoneNumber(phoneNumber);
        info.setRemainMoney(new BigDecimal(money));
        info.setOrderDesc(orderDesc);
        info.setStatus(status);
        return info;
    }

    private TransferRequest request(String phoneNumber, String money, String orderDesc) {
        TransferRequest request = new TransferRequest();
        request.setCellPhoneNumber(phoneNumber);
        request.setRemainMoney(new BigDecimal(money));
        request.setOrderDesc(orderDesc);
        request.setStatus(0);
        return request;
    }
}
