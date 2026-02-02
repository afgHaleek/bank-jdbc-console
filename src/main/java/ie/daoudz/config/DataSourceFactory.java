package ie.daoudz.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {

    public static DataSource create(DbConfig db) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(db.url());
        config.setUsername(db.username());
        config.setPassword(db.password());

        config.setMaximumPoolSize(db.poolSize());

        config.setInitializationFailTimeout(5_000);

        return new HikariDataSource(config);
    }
}
