package com.unicom.service.impl;

import com.unicom.dto.TransferRequest;
import com.unicom.dto.TransferResponse;
import com.unicom.entity.CellPhoneInfo;
import com.unicom.repository.CellPhoneInfoRepository;
import java.math.BigDecimal;
import javax.sql.DataSource;
import junit.framework.TestCase;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class TransferNumberServiceImplTest extends TestCase {
    private CellPhoneInfoRepository mobileRepository;
    private CellPhoneInfoRepository unicomRepository;

    @Override
    protected void setUp() {
        mobileRepository = new CellPhoneInfoRepository(createJdbcTemplate("mobile_service"));
        unicomRepository = new CellPhoneInfoRepository(createJdbcTemplate("unicom_service"));
    }

    public void testShouldTransferFromMobileToUnicom() {
        mobileRepository.insert(record("13311111111", "10.00", "old", 0));

        TransferNumberServiceImpl service = new TransferNumberServiceImpl(mobileRepository, unicomRepository);
        TransferResponse response = service.transferNumber(request("13311111111"));

        assertTrue(response.isSuccess());
        assertEquals(Integer.valueOf(1), mobileRepository.findByPhoneNumber("13311111111").getStatus());
        assertEquals(Integer.valueOf(0), unicomRepository.findByPhoneNumber("13311111111").getStatus());
        assertEquals("old", unicomRepository.findByPhoneNumber("13311111111").getOrderDesc());
        assertEquals(new BigDecimal("10.00"), unicomRepository.findByPhoneNumber("13311111111").getRemainMoney());
    }

    public void testShouldFailWhenSourceNumberMissing() {
        TransferNumberServiceImpl service = new TransferNumberServiceImpl(mobileRepository, unicomRepository);

        TransferResponse response = service.transferNumber(request("13399999999"));

        assertFalse(response.isSuccess());
        assertEquals("源运营商中不存在该号码。", response.getMessage());
        assertNull(unicomRepository.findByPhoneNumber("13399999999"));
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

    private CellPhoneInfo record(String phoneNumber, String money, String orderDesc, int status) {
        CellPhoneInfo info = new CellPhoneInfo();
        info.setCellPhoneNumber(phoneNumber);
        info.setRemainMoney(new BigDecimal(money));
        info.setOrderDesc(orderDesc);
        info.setStatus(status);
        return info;
    }

    private TransferRequest request(String phoneNumber) {
        TransferRequest request = new TransferRequest();
        request.setCellPhoneNumber(phoneNumber);
        return request;
    }
}
