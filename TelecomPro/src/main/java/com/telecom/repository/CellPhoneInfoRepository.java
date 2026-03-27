package com.telecom.repository;

import com.telecom.model.CellPhoneInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class CellPhoneInfoRepository {
    private static final RowMapper<CellPhoneInfo> ROW_MAPPER = (rs, rowNum) -> {
        CellPhoneInfo info = new CellPhoneInfo();
        info.setCellPhoneNumber(rs.getString("cell_phone_number"));
        info.setRemainMoney(rs.getBigDecimal("remain_money"));
        info.setOrderDesc(rs.getString("order_desc"));
        info.setStatus(rs.getInt("status"));
        return info;
    };

    private final JdbcTemplate jdbcTemplate;

    public CellPhoneInfoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CellPhoneInfo findByPhoneNumber(String phoneNumber) {
        String sql = "select cell_phone_number, remain_money, order_desc, status "
                + "from cell_phone_info where cell_phone_number = ? limit 1";
        return jdbcTemplate.query(sql, ROW_MAPPER, phoneNumber)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public int insert(CellPhoneInfo info) {
        String sql = "insert into cell_phone_info (cell_phone_number, remain_money, order_desc, status) "
                + "values (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                info.getCellPhoneNumber(),
                info.getRemainMoney(),
                info.getOrderDesc(),
                info.getStatus());
    }

    public int updateByPhoneNumber(CellPhoneInfo info) {
        String sql = "update cell_phone_info set remain_money = ?, order_desc = ?, status = ? "
                + "where cell_phone_number = ?";
        return jdbcTemplate.update(sql,
                info.getRemainMoney(),
                info.getOrderDesc(),
                info.getStatus(),
                info.getCellPhoneNumber());
    }
}
