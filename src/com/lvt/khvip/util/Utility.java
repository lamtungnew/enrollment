package com.lvt.khvip.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Utility {

    public static void closeObject(Connection cn) {
        try {
            if (cn != null) {
                cn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeObject(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeObject(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
