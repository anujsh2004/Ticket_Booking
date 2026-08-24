package com.ticketbooking.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/ticket_booking_db}")
    private String dbUrl;

    @Value("${spring.datasource.username:postgres}")
    private String defaultUsername;

    @Value("${spring.datasource.password:postgrespassword}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String url = dbUrl;
        String username = defaultUsername;
        String password = defaultPassword;

        // Auto-adapt cloud database URI formats (e.g. Render, Railway, Supabase, Neon)
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                String cleanUrl = url.replace("postgresql://", "http://").replace("postgres://", "http://");
                URI uri = new URI(cleanUrl);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }

                url = "jdbc:postgresql://" + host + ":" + port + path;
                log.info("Auto-adapted cloud DATABASE_URL to JDBC: {}", url);
            } catch (Exception e) {
                log.warn("Falling back to raw jdbc prefix for DATABASE_URL", e);
                if (!url.startsWith("jdbc:")) {
                    url = "jdbc:" + url;
                }
            }
        }

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
