package ie.daoudz.config;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

public class SchemaRunner {

    public static void run(DataSource ds, String schemaFile) {
        String sql = loadSql(schemaFile);

        try(Connection conn = ds.getConnection();
            Statement st = conn.createStatement()
        ) {
            st.execute(sql);
            System.out.println("Database schema initialized successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run schema", e);
        }
    }

    private static String loadSql(String filename) {

        if (filename == null) {
            throw new IllegalArgumentException("schema file cannot be null");
        }

        try{
            var inputStream = Objects.requireNonNull(
                    SchemaRunner.class
                            .getClassLoader()
                            .getResourceAsStream(filename), " Schema file not found in classpath: " + filename
            );

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream)
            )){

                return reader.lines().collect(Collectors.joining("\n"));

            }

        } catch (Exception e) {
            throw new RuntimeException("failed to load schema file",e);
        }
    }
}
