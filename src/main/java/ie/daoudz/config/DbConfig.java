package ie.daoudz.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfig {

    private final Properties props;

    public DbConfig(String propertiesFileName) {
        this.props = load(propertiesFileName);
    }

    private Properties load(String filename) {
        try(InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                throw new IllegalArgumentException("Config file not found in resource " + filename);
            }

            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + filename, e);
        }
    }

    public String url() {
        return props.getProperty("db.url");
    }

    public String username() {
        return props.getProperty("db.username");
    }

    public String password() {
        return props.getProperty("db.password");
    }

    public int poolSize() {
        return Integer.parseInt(props.getProperty("db.pool.size", "10"));
    }
}
