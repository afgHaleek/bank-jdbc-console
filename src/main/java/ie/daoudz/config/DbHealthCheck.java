package ie.daoudz.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DbHealthCheck {

    public static boolean canConnect(DataSource ds) {
        String sql = "SELECT 1";

        try(Connection conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        } catch (Exception e) {
            System.out.println("Db connection failed: " + e.getMessage());
            return false;
        }
    }
}
