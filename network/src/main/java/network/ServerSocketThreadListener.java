package network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {
    void onServerStart(ServerSocketThread thread);
    void onServerStop(ServerSocketThread thread);
    void onServerSocketCreated(ServerSocketThread thread, ServerSocket server);
    void onServerSoTimeout(ServerSocketThread thread, ServerSocket server);
    void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket client);
    void onServerException(ServerSocketThread thread, Throwable throwable);
}
