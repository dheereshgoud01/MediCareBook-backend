package com.medicarebook.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DB_HOST:#{null}}")
    private String dbHost;

    @Value("${DB_PORT:3306}")
    private String dbPort;

    @Value("${DB_NAME:medicarebook_db}")
    private String dbName;

    @Value("${DB_USER:root}")
    private String dbUser;

    @Value("${DB_PASSWORD:root}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // 1. Check environment variables for direct JDBC URL (e.g., from Render, TiDB, Aiven, or Railway)
        String envDbUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (envDbUrl == null || envDbUrl.isBlank()) {
            envDbUrl = System.getenv("DATABASE_URL");
        }
        if (envDbUrl == null || envDbUrl.isBlank()) {
            envDbUrl = System.getenv("MYSQL_URL");
        }

        if (envDbUrl != null && !envDbUrl.isBlank()) {
            envDbUrl = envDbUrl.trim();
            log.info("Configuring cloud database connection from environment URL: {}", maskUrl(envDbUrl));
            HikariConfig config = new HikariConfig();
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : System.getenv("MYSQL_USER");
            String pass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : System.getenv("MYSQL_PASSWORD");

            if (envDbUrl.startsWith("mysql://") || envDbUrl.startsWith("postgresql://") || envDbUrl.startsWith("postgres://")) {
                try {
                    java.net.URI uri = new java.net.URI(envDbUrl);
                    String scheme = (uri.getScheme().equalsIgnoreCase("postgres") || uri.getScheme().equalsIgnoreCase("postgresql"))
                            ? "postgresql"
                            : "mysql";
                    String host = uri.getHost();
                    int port = uri.getPort() != -1 ? uri.getPort() : (scheme.equals("postgresql") ? 5432 : 3306);
                    String path = (uri.getPath() != null && uri.getPath().startsWith("/")) ? uri.getPath().substring(1) : uri.getPath();
                    if (path == null || path.isBlank() || path.equalsIgnoreCase("sys")) {
                        path = "test";
                    }

                    if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        if (userInfo[0] != null && !userInfo[0].isBlank()) user = userInfo[0];
                        if (userInfo.length > 1 && !userInfo[1].isBlank()) pass = userInfo[1];
                    }

                    String query = uri.getQuery() != null ? "?" + uri.getQuery() : (scheme.equals("mysql") ? "?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true" : "");
                    envDbUrl = String.format("jdbc:%s://%s:%d/%s%s", scheme, host, port, path, query);
                    if (scheme.equals("postgresql")) {
                        config.setDriverClassName("org.postgresql.Driver");
                    } else {
                        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    }
                } catch (Exception parseEx) {
                    log.warn("Could not parse cloud URI, prepending jdbc:: {}", parseEx.getMessage());
                    if (!envDbUrl.startsWith("jdbc:")) {
                        envDbUrl = "jdbc:" + envDbUrl;
                    }
                }
            } else if (!envDbUrl.startsWith("jdbc:")) {
                envDbUrl = "jdbc:mysql://" + envDbUrl;
            }

            config.setJdbcUrl(envDbUrl);
            if (user != null && !user.isBlank()) config.setUsername(user);
            if (pass != null && !pass.isBlank()) config.setPassword(pass);
            config.setInitializationFailTimeout(4000);
            config.setConnectionTimeout(4000);

            try {
                HikariDataSource ds = new HikariDataSource(config);
                try (var conn = ds.getConnection()) {
                    log.info("Successfully connected to cloud database at {}!", maskUrl(envDbUrl));
                    return ds;
                }
            } catch (Exception e) {
                log.error("Failed to connect to cloud database at {}. Falling back to embedded database: {}", maskUrl(envDbUrl), e.getMessage());
                return createEmbeddedDataSource();
            }
        }

        // 2. If explicit external DB_HOST is defined (not localhost), connect to it
        if (dbHost != null && !dbHost.isBlank() && !dbHost.equalsIgnoreCase("localhost") && !dbHost.equalsIgnoreCase("127.0.0.1")) {
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    dbHost, dbPort, dbName);
            log.info("Configuring external MySQL connection to {}:{}", dbHost, dbPort);
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser != null ? dbUser : "root");
            config.setPassword(dbPassword != null ? dbPassword : "root");
            config.setInitializationFailTimeout(4000);
            config.setConnectionTimeout(4000);
            try {
                HikariDataSource ds = new HikariDataSource(config);
                try (var conn = ds.getConnection()) {
                    log.info("Connected to external MySQL database on {}:{}", dbHost, dbPort);
                    return ds;
                }
            } catch (Exception e) {
                log.error("Failed to connect to external MySQL on {}:{}. Falling back to embedded database: {}", dbHost, dbPort, e.getMessage());
                return createEmbeddedDataSource();
            }
        }

        // 3. Test local MySQL (used for local PC development)
        boolean isRender = "true".equalsIgnoreCase(System.getenv("RENDER"));
        if (!isRender) {
            try {
                String localMysqlUrl = String.format("jdbc:mysql://localhost:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        dbPort, dbName);
                HikariConfig localConfig = new HikariConfig();
                localConfig.setJdbcUrl(localMysqlUrl);
                localConfig.setUsername(dbUser != null ? dbUser : "root");
                localConfig.setPassword(dbPassword != null ? dbPassword : "root");
                localConfig.setInitializationFailTimeout(2000);
                localConfig.setConnectionTimeout(2000);
                HikariDataSource ds = new HikariDataSource(localConfig);
                try (var conn = ds.getConnection()) {
                    log.info("Connected to local MySQL database on port {}", dbPort);
                    return ds;
                }
            } catch (Exception e) {
                log.warn("Local MySQL on port {} unreachable. Falling back to embedded database: {}", dbPort, e.getMessage());
            }
        }

        // 4. Default for Render and Cloud Containers with zero external database setup:
        return createEmbeddedDataSource();
    }

    private DataSource createEmbeddedDataSource() {
        log.info("Initializing embedded MySQL-compatible database for cloud deployment (zero external dependency).");
        HikariConfig h2Config = new HikariConfig();
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setJdbcUrl("jdbc:h2:mem:medicarebook_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        return new HikariDataSource(h2Config);
    }

    private String maskUrl(String url) {
        return url.replaceAll("(?<=:)[^@/:]+(?=@)", "******");
    }
}
