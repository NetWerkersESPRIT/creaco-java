package utils;

import java.sql.*;

public class DbDump {
    public static void main(String[] args) {
        try {
            Connection con = MyConnection.getInstance().getConnection();
            System.out.println("--- DUMPING user_cours_progress ---");
            dumpTable(con, "user_cours_progress");
            System.out.println("\n--- DUMPING user_resource_completion ---");
            dumpTable(con, "user_resource_completion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dumpTable(Connection con, String table) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) System.out.print(meta.getColumnName(i) + "\t");
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) System.out.print(rs.getObject(i) + "\t");
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error dumping " + table + ": " + e.getMessage());
        }
    }
}
