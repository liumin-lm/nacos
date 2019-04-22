package com.alibaba.nacos.config.server.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.config.server.service.BasicDataSourceServiceImpl.defaultIfNull;


@Component
public class DynamicDataSourceCP {


    @Autowired
    Environment env;


    public DataSource getDataSource(String jdbcDriverName, String jdbcUrl, String userName, String password){

        String val = env.getProperty("db.datasourceCP");


        if(null!=val && "dhcp".equals(val.trim())){
            return getDataSourceUsingDHCP(jdbcDriverName, jdbcUrl, userName, password);
        }
        return getDatasourceUsingHikariCP(jdbcDriverName, jdbcUrl, userName, password);
    }
    public DataSource getDataSourceUsingDHCP(String jdbcDriverName, String jdbcUrl, String userName, String password){
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(jdbcDriverName);

        ds.setUrl(jdbcUrl);
        ds.setUsername(userName);
        ds.setPassword(password);

        String val = null;
        val = env.getProperty("db.initialSize");
        ds.setInitialSize(Integer.parseInt(defaultIfNull(val, "10")));

        val = env.getProperty("db.maxActive");
        ds.setMaxActive(Integer.parseInt(defaultIfNull(val, "20")));

        val = env.getProperty("db.maxIdle");
        ds.setMaxIdle(Integer.parseInt(defaultIfNull(val, "50")));

        ds.setMaxWait(3000L);
        ds.setPoolPreparedStatements(true);

        // 每10分钟检查一遍连接池
        ds.setTimeBetweenEvictionRunsMillis(TimeUnit.MINUTES
            .toMillis(10L));
        ds.setTestWhileIdle(true);
        ds.setValidationQuery("SELECT 1 FROM dual");
        return ds;
    }
    public DataSource getDatasourceUsingHikariCP(String jdbcDriverName, String jdbcUrl, String userName, String password){
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(jdbcDriverName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(userName);
        config.setPassword(password);
        config.setMaximumPoolSize(50);
        config.setMaxLifetime(10000L);
        HikariDataSource ds = new HikariDataSource(config);

        return ds;
    }
}
