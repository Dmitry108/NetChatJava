package client.core;

import client.gui.ChatClientView;
import common.NChMP;
import network.SocketThread;
import network.SocketThreadListener;

import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

public class ChatClientImpl implements ChatClient, SocketThreadListener {

    private ChatClientView view;

    private SocketThread socketThread;

    public ChatClientImpl(ChatClientView view) {
        this.view = view;
    }

    @Override
    public void connect(String ip, String port) {
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            onException(Thread.currentThread(), e);
        }
    }

    @Override
    public void disconnect() {
        socketThread = null;
    }

    @Override
    public void sendMessage(String login, String message) {
        socketThread.sendMessage(NChMP.getMessageBroadcast(login, message));
    }

    private void writeMessageToFile(String text) {
        try (FileWriter out = new FileWriter("log_chat.txt", true)) {
            out.write(text + "\n");
            out.flush();
        } catch (IOException e) {
            onException(Thread.currentThread(), e);
        }
    }

    @Override
    public void tryToLogin(String login, String password) {
        socketThread.sendMessage(NChMP.getAuthRequest(login, password));
    }

    @Override
    public void onException(Thread thread, Throwable throwable){
        String message = throwable.getStackTrace().length == 0 ? "Empty stack trace" :
                String.format("Exception in thread %s %s: %s%n%s", thread.getName(),
                        throwable.getClass().getCanonicalName(), throwable.getMessage(),
                        throwable.getStackTrace()[0]);
        view.showException(message);
    }

    @Override
    public void onSockedStart(SocketThread thread, Socket socket) {
        view.putLog("Start");
    }

    @Override
    public void onSockedStop(SocketThread thread) {
//        putLog("Stop");
        view.onDisconnect();
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
//        putLog("Ready");
        view.onConnect();
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String message) {
        view.putLog(message);
        writeMessageToFile(message);
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        onException(thread, throwable);
    }
}
