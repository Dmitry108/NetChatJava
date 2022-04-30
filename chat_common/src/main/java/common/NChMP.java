package common;

public class NChMP {
    public static final String START = "/"; // /|~begin service string~/|";
    public static final String DELIMITER = "§"; // "/|~delimiter~|/";

    public static final String AUTH_REQUEST = "auth_request";
    public static final String AUTH_ACCEPT = "auth_accept";
    public static final String AUTH_DENY = "auth_deny";
    public static final String MESSAGE_BROADCAST = "message_broadcast";
    public static final String MESSAGE_FORMAT_ERROR = "message_error";

    public static String getAuthRequest(String login, String password) {
        return START + AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return START + AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return START + AUTH_DENY;
    }

    public static String getMessageFormatError(String message) {
        return START + MESSAGE_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getMessageBroadcast(String source, String message) {
        return START + MESSAGE_BROADCAST + DELIMITER + System.currentTimeMillis() + DELIMITER + source +
                DELIMITER + message;
    }
}