package network;

import java.net.Socket;

public interface SocketThreadListener {
    void onSockedStart(SocketThread thread, Socket socket);
    void onSockedStop(SocketThread thread);
    void onSocketReady(SocketThread thread, Socket socket);
    void onReceiveString(SocketThread thread, Socket socket, String message);
    void onSocketThreadException(SocketThread thread, Throwable throwable);
}
