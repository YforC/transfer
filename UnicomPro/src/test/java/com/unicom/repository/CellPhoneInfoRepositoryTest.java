package com.unicom.repository;

import com.unicom.entity.CellPhoneInfo;
import java.math.BigDecimal;
import javax.sql.DataSource;
import junit.framework.TestCase;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class CellPhoneInfoRepositoryTest extends TestCase {
    private JdbcTemplate jdbcTemplate;
    private CellPhoneInfoRepository repository;

    @Override
    protected void setUp() {
        jdbcTemplate = new JdbcTemplate(createDataSource());
        jdbcTemplate.execute("drop table if exists cell_phone_info");
        jdbcTemplate.execute("create table cell_phone_info ("
                + "id bigint auto_increment primary key, "
                + "cell_phone_number varchar(20), "
                + "remain_money decimal(10,2), "
                + "order_desc varchar(100), "
                + "status tinyint)");
        repository = new CellPhoneInfoRepository(jdbcTemplate);
    }

    public void testShouldFindInsertAndUpdateByPhoneNumber() {
        assertNull(repository.findByPhoneNumber("13300000000"));

        CellPhoneInfo created = new CellPhoneInfo();
        created.setCellPhoneNumber("13300000000");
        created.setRemainMoney(new BigDecimal("10.00"));
        created.setOrderDesc("A");
        created.setStatus(0);
        repository.insert(created);

        CellPhoneInfo loaded = repository.findByPhoneNumber("13300000000");
        assertEquals("13300000000", loaded.getCellPhoneNumber());

        created.setRemainMoney(new BigDecimal("20.00"));
        created.setOrderDesc("B");
        created.setStatus(1);
        repository.updateByPhoneNumber(created);

        CellPhoneInfo updated = repository.findByPhoneNumber("13300000000");
        assertEquals(new BigDecimal("20.00"), updated.getRemainMoney());
        assertEquals("B", updated.getOrderDesc());
        assertEquals(Integer.valueOf(1), updated.getStatus());
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:unicom_repo;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
