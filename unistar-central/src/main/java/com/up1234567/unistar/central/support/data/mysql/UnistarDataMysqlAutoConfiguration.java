package com.up1234567.unistar.central.support.data.mysql;


import com.up1234567.unistar.central.support.core.UnistarProperties;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.UnistarDataProperties;
import com.up1234567.unistar.common.IUnistarConst;
import com.zaxxer.hikari.HikariDataSource;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@CustomLog(topic = IUnistarConst.DEFAULT_LOGGER_UNISTAR)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = UnistarDataProperties.TYPE, havingValue = "mysql")
@EnableConfigurationProperties({UnistarProperties.class, UnistarDataProperties.class})
public class UnistarDataMysqlAutoConfiguration {

    @Bean
    public DataSource dataSource(UnistarDataProperties unistarDataProperties) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(unistarDataProperties.getUri());
        if (StringUtils.isNotEmpty(unistarDataProperties.getUsername())) {
            ds.setUsername(unistarDataProperties.getUsername());
            ds.setPassword(unistarDataProperties.getPassword());
        }
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public IUnistarDao unistarDao(JdbcTemplate jdbcTemplate) {
        log.debug("use mysql storage");
        return new UnistarMysqlDao(jdbcTemplate);
    }

}
