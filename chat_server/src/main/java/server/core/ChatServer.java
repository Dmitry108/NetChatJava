package server.core;

import common.NChMP;
import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {
    private final int SERVER_SOCKET_TIMEOUT = 2000;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss ");
    private ChatServerListener listener;
    private ServerSocketThread server;
    private Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server != null && server.isAlive()) {
            putLog("Server already started");
        } else {
//            putLog("Server started at port " + port);
            server = new ServerSocketThread(this, "Chat server", port, SERVER_SOCKET_TIMEOUT);
        }
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            putLog("Server is not running");
        } else {
//            putLog("Server stopped");
            server.interrupt();
        }
    }

    private void putLog(String message) {
        listener.onChatServerMessage(String.format("%s %s: %s", DATE_FORMAT.format(System.currentTimeMillis()),
                Thread.currentThread().getName(), message));
    }

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server thread started");
        ClientsDBProvider.connect();
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
        ClientsDBProvider.disconnect();
        clients.forEach(SocketThread::close);
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");
    }

    @Override
    public void onServerSoTimeout(ServerSocketThread thread, ServerSocket server) {

    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket client) {
        putLog("Client connected");
        String name = "SocketThread " + client.getInetAddress() + ": " + client.getPort();
        new ClientThread(this, name, client);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public synchronized void onSockedStart(SocketThread thread, Socket socket) {
        putLog("Client connected");
    }

    @Override
    public synchronized void onSockedStop(SocketThread thread) {
//        putLog("Client disconnected");
        ClientThread client = (ClientThread) thread;
        clients.remove(client);
        //специальное служебное сообщение
        if (client.getIsAuth() && !client.isReconnection()) {
            sendToAllAuthorizes(NChMP.getMessageBroadcast("Server", client.getNickname() + " disconnected"));
        }
        sendToAllAuthorizes(NChMP.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String message) {
        ClientThread client = (ClientThread) thread;
        if (client.getIsAuth()) {
            handleAuthMessage(client, message);
        } else {
            handleNotAuthMessage(client, message);
        }
    }

    public void handleAuthMessage(ClientThread clientThread, String message) {
        System.out.println(message);
        String[] strArray = message.split(NChMP.DELIMITER, 3);
        switch (strArray[0]) {
            case NChMP.USER_BROADCAST -> sendToAllAuthorizes(
                    NChMP.getMessageBroadcast(clientThread.getNickname(), strArray[1]));
            case NChMP.USER_PRIVATE -> sendPrivate(
                    NChMP.getMessagePrivate(clientThread.getNickname(), strArray[1]), strArray[2]);
            default -> clientThread.messageFormatError(message);
        }
    }

    private void sendToAllAuthorizes(String message) {
        clients.forEach(client -> {
            if (((ClientThread) client).getIsAuth()) {
                client.sendMessage(message);
            }
        });
    }

    private void sendPrivate(String message, String nicknames) {
        List<String> strNick = Arrays.asList(nicknames.split(NChMP.DELIMITER));
        clients.forEach(client -> {
            ClientThread clientThread = (ClientThread) client;
            if (clientThread.getIsAuth() && strNick.contains(clientThread.getNickname())) {
                client.sendMessage(message);
            }
        });
    }

    private void handleNotAuthMessage(ClientThread clientThread, String message) {
        String[] strArray = message.split(NChMP.DELIMITER);
        if (strArray.length != 3 ||
                !strArray[0].equals(NChMP.AUTH_REQUEST)) {
            clientThread.messageFormatError(message);
            return;
        }
        String login = strArray[1];
        String password = strArray[2];
        String nickname = ClientsDBProvider.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid login attempt " + login);
            clientThread.authFail();
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);
            clientThread.authAccept(nickname);
            if (oldClient == null) {
                sendToAllAuthorizes(NChMP.getMessageBroadcast("Server", nickname + " connected"));
            } else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }
        }
        sendToAllAuthorizes(NChMP.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketThreadException(SocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
    }

    private String getUsers() {
        final StringBuilder sb = new StringBuilder();
        clients.forEach(thread -> {
            ClientThread client = (ClientThread) thread;
            if (client.getIsAuth()) {
                sb.append(client.getNickname()).append(NChMP.DELIMITER);
            }
        });
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        ClientThread client;
        for (SocketThread socketThread : clients) {
            client = (ClientThread) socketThread;
            if (!client.getIsAuth()) continue;
            if (client.getNickname().equals(nickname)) {
                return client;
            }
        }
        return null;
    }
}