package com.telecom.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DataSourceConfig.class)
            .withPropertyValues(
                    "mobile.datasource.url=jdbc:h2:mem:mobile_cfg;MODE=MySQL;DB_CLOSE_DELAY=-1",
                    "mobile.datasource.driver-class-name=org.h2.Driver",
                    "mobile.datasource.username=sa",
                    "mobile.datasource.password=",
                    "unicom.datasource.url=jdbc:h2:mem:unicom_cfg;MODE=MySQL;DB_CLOSE_DELAY=-1",
                    "unicom.datasource.driver-class-name=org.h2.Driver",
                    "unicom.datasource.username=sa",
                    "unicom.datasource.password=");

    @Test
    void shouldCreateBothDataSourcesFromUrlProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("mobileDataSource");
            assertThat(context).hasBean("unicomDataSource");
        });
    }
}
