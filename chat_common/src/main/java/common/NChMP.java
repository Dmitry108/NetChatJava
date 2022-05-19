package common;

public class NChMP {
    public static final String START = "/"; // /|~begin service string~/|";
    public static final String DELIMITER = "§"; // "/|~delimiter~|/";

    public static final String AUTH_REQUEST = START + "auth_request";
    public static final String AUTH_ACCEPT = START + "auth_accept";
    public static final String AUTH_DENY = START + "auth_deny";
    public static final String MESSAGE_BROADCAST = START + "message_broadcast";
    public static final String MESSAGE_FORMAT_ERROR = START + "message_error";

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
}