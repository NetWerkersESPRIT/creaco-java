package utils;

import java.sql.*;

public class UserDump {
    public static void main(String[] args) {
        try {
            Connection con = MyConnection.getInstance().getConnection();
            System.out.println("--- DUMPING users ---");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, username, image FROM users WHERE id IN (7, 9)");
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) System.out.print(meta.getColumnLabel(i) + "\t");
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) System.out.print(rs.getString(i) + "\t");
                System.out.println();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
