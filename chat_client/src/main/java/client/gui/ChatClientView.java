package client.gui;

public interface ChatClientView {
    void putLog(String message);
    void showException(String message);
    void onConnect();
    void onDisconnect();
}
