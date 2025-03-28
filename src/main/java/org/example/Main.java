package org.example;

import org.example.database.DBConnectionPool;

import java.sql.*;

public class Main {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS= "postgres";
    private static int POOL_SIZE = 20;

    public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
        Class.forName("org.postgresql.Driver");

        DBConnectionPool dbConnectionPool = new DBConnectionPool(DB_URL, DB_USER, DB_PASS, POOL_SIZE);

        // populates the flight data to be ready for checkins.
        // ingestData();

        doAirlineFlightCheckin(dbConnectionPool, 1);  // filled - random (12), time 80ms
        printFlightLayout(1);
        clearCheckins(dbConnectionPool);
        Thread.sleep(2 * 1000);

       /*doAirlineFlightCheckin(dbConnectionPool, 2);  // filled - all (120), time 160ms
        printFlightLayout(2);
        clearCheckins(dbConnectionPool);
        Thread.sleep(2 * 1000);

        doAirlineFlightCheckin(dbConnectionPool, 3);  // filled - all (120), time 70ms
        printFlightLayout(3);
        clearCheckins(dbConnectionPool);*/
    }

    private static void clearCheckins(DBConnectionPool dbConnectionPool) {
        Connection connection = dbConnectionPool.getConnection();

        try {
            String query = "UPDATE flight set user_id = null";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            dbConnectionPool.returnConnection(connection);
        }
    }

    private static void doAirlineFlightCheckin(DBConnectionPool dbConnectionPool, int approach) {
        int threadCount = 120;
        Thread[] threads = new Thread[threadCount];
        Connection connection = null;
        int t = 0;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String query = "SELECT * FROM airlineusers ORDER BY id";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String userName = resultSet.getString("name");
                if (approach == 1)
                    threads[t++] = new Thread(new Solution1(dbConnectionPool, userId, userName));
                else if (approach == 2)
                    threads[t++] = new Thread(new Solution2(dbConnectionPool, userId, userName));
                else if (approach == 3)
                    threads[t++] = new Thread(new Solution3(dbConnectionPool, userId, userName));
            }

            long start = System.currentTimeMillis();
            for (int i = 0; i < t; ++i)
                threads[i].start();

            for (int i = 0; i < t; ++i) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                }
            }
            long timeTaken = System.currentTimeMillis() - start;

            System.out.println(" Time taken(ms): " + timeTaken + " approach " + approach + "\n\n");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void ingestData() throws SQLException {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String query = "INSERT INTO flight (trip, seat, user_id) VALUES(1, ?, NULL)";

        try {
            for (int i = 1; i <= 20; ++i) {
                PreparedStatement preparedStatement1 = connection.prepareStatement(query);
                PreparedStatement preparedStatement2 = connection.prepareStatement(query);
                PreparedStatement preparedStatement3 = connection.prepareStatement(query);
                PreparedStatement preparedStatement4 = connection.prepareStatement(query);
                PreparedStatement preparedStatement5 = connection.prepareStatement(query);
                PreparedStatement preparedStatement6 = connection.prepareStatement(query);

                preparedStatement1.setString(1, i + "-A");
                preparedStatement2.setString(1, i + "-B");
                preparedStatement3.setString(1, i + "-C");
                preparedStatement4.setString(1, i + "-D");
                preparedStatement5.setString(1, i + "-E");
                preparedStatement6.setString(1, i + "-F");

                preparedStatement1.executeUpdate();
                preparedStatement2.executeUpdate();
                preparedStatement3.executeUpdate();
                preparedStatement4.executeUpdate();
                preparedStatement5.executeUpdate();
                preparedStatement6.executeUpdate();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                connection.close();
        }
    }

    public static void printFlightLayout(int approach) throws SQLException {
        System.out.println("Approach " + approach);

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n A  B  C    D  E  F ");

        try {
            String query = "SELECT * FROM flight ORDER BY id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            int i = 0;
            int row = 1;

            while (resultSet.next()) {
                int checkedInUser = resultSet.getInt("user_id");

                if (checkedInUser != 0)
                    System.out.print(" X ");   // seat taken
                else
                    System.out.print(" . ");   // seat not taken

                ++i;
                if (i % 3 == 0)
                    System.out.print("  ");
                if (i % 6 == 0)
                    System.out.println("  [" + (row++) + "]");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                connection.close();
        }
    }
}