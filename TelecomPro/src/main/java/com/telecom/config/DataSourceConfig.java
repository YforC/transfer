package com.telecom.config;

import com.telecom.repository.CellPhoneInfoRepository;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfig {
    @Bean("mobileDataSource")
    @ConfigurationProperties("mobile.datasource")
    public DataSource mobileDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("unicomDataSource")
    @ConfigurationProperties("unicom.datasource")
    public DataSource unicomDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("mobileJdbcTemplate")
    public JdbcTemplate mobileJdbcTemplate(@Qualifier("mobileDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("unicomJdbcTemplate")
    public JdbcTemplate unicomJdbcTemplate(@Qualifier("unicomDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("mobileCellPhoneInfoRepository")
    public CellPhoneInfoRepository mobileCellPhoneInfoRepository(
            @Qualifier("mobileJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new CellPhoneInfoRepository(jdbcTemplate);
    }

    @Bean("unicomCellPhoneInfoRepository")
    public CellPhoneInfoRepository unicomCellPhoneInfoRepository(
            @Qualifier("unicomJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new CellPhoneInfoRepository(jdbcTemplate);
    }
}
