package server.core;

import common.NChMP;

import java.sql.*;

public class ClientsDBProvider {
    private static Connection connection;
    private static PreparedStatement statement;

    private static final String AUTH_QUERY = "SELECT nickname FROM users WHERE login = ? AND password = ?;";
    private static final String LOGIN_EXISTS = "SELECT login FROM users WHERE login = ?;";
    private static final String NICKNAME_EXISTS = "SELECT nickname FROM users WHERE nickname = ?;";
    private static final String REGISTER_QUERY = "INSERT INTO users (login, nickname, password) VALUES (?, ?, ?);";
    private static final String UPDATE_NICKNAME = "UPDATE users SET nickname = ? WHERE login = ?;";

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat_server/clients.db");
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
        try {
            statement = connection.prepareStatement(AUTH_QUERY);
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet request = statement.executeQuery();
            System.out.println(request.toString());
            if (request.next()) {
                return request.getString("nickname");
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return null;
    }

    synchronized public static String register(String login, String nickname, String password) {
        try {
            boolean isLoginValid = checkLoginExists(login);
            boolean isNicknameValid = checkNicknameExists(nickname);
            statement = connection.prepareStatement(REGISTER_QUERY);
            statement.setString(1, login);
            statement.setString(2, nickname);
            statement.setString(3, password);
//            if (!isLoginValid) {
//                if (!isNicknameValid) return NChMP.LOGIN_NICKNAME_EXISTS;
//                else return NChMP.LOGIN_EXISTS;
//            } else {
//                if (!isNicknameValid) return NChMP.NICKNAME_EXISTS;
//                else return (statement.executeUpdate() != 0) ? NChMP.ACCESS : null;
//            }
            if (isLoginValid && isNicknameValid) return (statement.executeUpdate() != 0) ? NChMP.ACCESS : null;
            if (!isLoginValid && !isNicknameValid) return NChMP.LOGIN_NICKNAME_EXISTS;
            if (!isLoginValid) return NChMP.LOGIN_EXISTS;
            return NChMP.NICKNAME_EXISTS;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    synchronized private static boolean checkLoginExists(String login) {
        try {
            statement = connection.prepareStatement(LOGIN_EXISTS);
            statement.setString(1, login);
            ResultSet request = statement.executeQuery();
            return !request.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized private static boolean checkNicknameExists(String nickname) {
        try {
            statement = connection.prepareStatement(NICKNAME_EXISTS);
            statement.setString(1, nickname);
            ResultSet request = statement.executeQuery();
            return !request.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    synchronized public static boolean updateNickname(String login, String nickname) {
        if (!checkNicknameExists(nickname)) return false;
        try {
            statement = connection.prepareStatement(UPDATE_NICKNAME);
            statement.setString(1, nickname);
            statement.setString(2, login);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}