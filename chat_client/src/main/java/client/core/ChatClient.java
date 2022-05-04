package client.core;

public interface ChatClient {
    void sendMessage(String login, String message);
    void tryToLogin(String login, String password);
    void connect(String ip, String port);
    void disconnect();
    void onException(Thread thread, Throwable throwable);
}
