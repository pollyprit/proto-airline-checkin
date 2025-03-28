package org.example;

import org.example.database.DBConnectionPool;

import java.sql.*;

public class Solution3 extends Thread {
    int userId;
    private final String userName;
    DBConnectionPool dbConnectionPool;

    public Solution3(DBConnectionPool dbConnectionPool, int userId, String userName) {
        this.dbConnectionPool = dbConnectionPool;
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public void run() {
        Connection connection = dbConnectionPool.getConnection();

        try {
            connection.setAutoCommit(false);

            String query = "SELECT * FROM flight " +
                    " WHERE trip = 1 and user_id is null" +
                    " ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String seat = resultSet.getString("seat");

                System.out.println("Seat " + seat + " selected by " + this.userName);

                query = "UPDATE flight SET user_id = ? " +
                        " WHERE trip = 1 and user_id is null and id = ?";

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, this.userId);
                preparedStatement.setInt(2, id);

                int booked = preparedStatement.executeUpdate();
                if (booked == 1)
                    System.out.println("Seat " + seat + " booked by   " + this.userName);

                connection.commit();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            dbConnectionPool.returnConnection(connection);
        }
    }
}
