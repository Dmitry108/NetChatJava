package server.core;

import common.NChMP;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuth;
    private boolean isReconnection;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public String getNickname() {
        return nickname;
    }

    public boolean getIsAuth() {
        return isAuth;
    }

    public void authAccept(String nickname) {
        this.nickname = nickname;
        this.isAuth = true;
        sendMessage(NChMP.getAuthAccept(nickname));
    }

    public void authFail() {
        sendMessage(NChMP.getAuthDenied());
        close();
    }

    public void registerResponse(String responseCode) {
        if (NChMP.ACCESS.equals(responseCode)) {
            sendMessage(NChMP.getRegisterAccess());
        } else {
            sendMessage(NChMP.getRegisterDeny(responseCode));
        }
    }

    public void messageFormatError(String message) {
        sendMessage(NChMP.getMessageFormatError(message));
        close();
    }

    public boolean isReconnection() {
        return isReconnection;
    }

    public void reconnect() {
        isReconnection = true;
        close();
    }
}