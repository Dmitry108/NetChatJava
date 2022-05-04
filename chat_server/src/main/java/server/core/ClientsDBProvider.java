package server.core;

import java.sql.*;

public class ClientsDBProvider {
    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat_server/clients.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    synchronized static String getNickname(String login, String password) {
        String query = String.format("SELECT nickname FROM users WHERE login = '%s' AND password = '%s'", login, password);
        try (ResultSet request = statement.executeQuery(query)) {
            if (request.next()) {
                return request.getString("nickname");
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return null;
    }
}