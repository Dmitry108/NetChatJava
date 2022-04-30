package server.core;

import common.NChMP;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuth;

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
        //close();
    }

    public void messageFormatError(String message) {
        sendMessage(NChMP.getMessageFormatError(message));
        //close();
    }


}
