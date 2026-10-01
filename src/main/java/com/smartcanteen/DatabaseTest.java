package com.smartcanteen;

import com.smartcanteen.util.DatabaseUtil;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try {
            Connection connection = DatabaseUtil.getConnection();

            System.out.println("SUCCESS: Database connected!");

            connection.close();

        } catch (Exception e) {
            System.out.println("FAILED: Database connection failed.");
            e.printStackTrace();
        }
    }
}
