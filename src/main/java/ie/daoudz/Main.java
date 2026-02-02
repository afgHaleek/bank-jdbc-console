package ie.daoudz;

import ie.daoudz.app.MainMenu;
import ie.daoudz.config.DataSourceFactory;
import ie.daoudz.config.DbConfig;
import ie.daoudz.config.DbHealthCheck;

import javax.sql.DataSource;

public class Main {
    public static void main(String[] args) {


        System.out.println("=== Bank Transaction Processing System ===");

        DbConfig config = new DbConfig("application.properties");
        DataSource ds = DataSourceFactory.create(config);

        if (!DbHealthCheck.canConnect(ds)) {
            System.out.println("DB not reachable. Exiting.");
            return;
        }

        new MainMenu(ds).start();

    }
}