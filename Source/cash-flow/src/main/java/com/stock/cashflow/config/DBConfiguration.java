package com.stock.cashflow.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.stock.cashflow.persistence.repository"})
@ConfigurationProperties(prefix = "spring.datasource.hikari")
@Slf4j
public class DBConfiguration extends HikariConfig {

    @Autowired
    private Environment env;

    private static final HikariConfig config = new HikariConfig();

    @Bean
    public DataSource authConfigDatasource() {

        config.setJdbcUrl(env.getProperty("spring.datasource.hikari.jdbc-url"));
        config.setUsername(env.getProperty("spring.datasource.hikari.username"));
        config.setPassword(env.getProperty("spring.datasource.hikari.password"));
        return new HikariDataSource(config);
    }

}
