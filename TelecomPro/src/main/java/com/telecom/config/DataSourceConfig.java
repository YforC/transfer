package com.telecom.config;

import com.telecom.repository.CellPhoneInfoRepository;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties("mobile.datasource")
    public DataSourceProperties mobileDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("unicom.datasource")
    public DataSourceProperties unicomDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("mobileDataSource")
    public DataSource mobileDataSource(
            @Qualifier("mobileDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean("unicomDataSource")
    public DataSource unicomDataSource(
            @Qualifier("unicomDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
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
