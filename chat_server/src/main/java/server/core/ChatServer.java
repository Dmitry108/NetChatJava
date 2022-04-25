package server.core;

import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {
    private final int SERVER_SOCKET_TIMEOUT = 2000;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss ");
    private ServerSocketThread server;

    public void start(int port) {
        if (server != null && server.isAlive()) {
            System.out.println("Server already started");
        } else {
            System.out.println("Server started at port " + port);
            server = new ServerSocketThread(this, "Chat server", port, SERVER_SOCKET_TIMEOUT);
        }
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            System.out.println("Server is not running");
        } else {
//            System.out.println("Server stopped");
            server.interrupt();
        }
    }

    private void putLog(String message) {
        System.out.printf("%s %s: %s%n", DATE_FORMAT.format(System.currentTimeMillis()),
                Thread.currentThread().getName(), message);
    }

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server thread started");
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        System.out.println("Server socket created");
    }

    @Override
    public void onServerSoTimeout(ServerSocketThread thread, ServerSocket server) {

    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket client) {
        putLog("Client connected");
        String name = "SocketThread " + client.getInetAddress() + ": " + client.getPort();
        new SocketThread(this, name, client);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onSockedStart(SocketThread thread, Socket socket) {
        putLog("Client connected");
    }

    @Override
    public void onSockedStop(SocketThread thread) {
        putLog("Client disconnected");
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready");
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String message) {
        thread.sendMessage("echo: " + message);
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }
}