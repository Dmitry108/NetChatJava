package common;

public class NChMP {
    public static final String START = "/"; // /|~begin service string~/|";
    public static final String DELIMITER = "§"; // "/|~delimiter~|/";

    public static final String AUTH_REQUEST = START + "auth_request";
    public static final String AUTH_ACCEPT = START + "auth_accept";
    public static final String AUTH_DENY = START + "auth_deny";
    public static final String MESSAGE_BROADCAST = START + "message_broadcast";
    public static final String MESSAGE_PRIVATE = START + "message_private";
    public static final String MESSAGE_FORMAT_ERROR = START + "message_error";
    public static final String USER_LIST = START + "user_list";
    public static final String USER_BROADCAST = START + "user_broadcast";
    public static final String USER_PRIVATE = START + "user_private";



    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENY;
    }

    public static String getMessageFormatError(String message) {
        return MESSAGE_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getMessageBroadcast(String source, String message) {
        return MESSAGE_BROADCAST + DELIMITER + System.currentTimeMillis() + DELIMITER + source +
                DELIMITER + message;
    }

    public static String getMessagePrivate(String source, String message) {
        return MESSAGE_PRIVATE + DELIMITER + System.currentTimeMillis() + DELIMITER + source +
                DELIMITER + message;
    }

    public static String getClientBroadcast(String message) {
        return USER_BROADCAST + DELIMITER + message;
    }

    public static String getClientPrivate(String message, String nicknames) {
        return USER_PRIVATE + DELIMITER + message + DELIMITER + nicknames;
    }

    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }
}