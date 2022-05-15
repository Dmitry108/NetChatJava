package client;

import common.NChMP;
import network.SocketThread;
import network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss ");
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    public static final String TITLE = "Chat";

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField ipAddressTextField = new JTextField("127.0.0.1");
    private final JTextField portTextField = new JTextField("222");
    private final JCheckBox onTopCheckBox = new JCheckBox("Always on top");
    private final JTextField loginTextField = new JTextField("aglar");
    private final JPasswordField passwordField = new JPasswordField("123");
    private final JButton loginButton = new JButton("Login");
    private final JTextArea logTextArea = new JTextArea();
    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton logoutButton = new JButton("Logout");
    private final JTextField messageTextField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final JList<String> usersList = new JList<>();

    private boolean shownIoErrors = false;
    private SocketThread socketThread;

    public ClientGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);

        logTextArea.setEditable(false);
        logTextArea.setLineWrap(true);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        JScrollPane userListScrollPane = new JScrollPane(usersList);
        userListScrollPane.setPreferredSize(new Dimension(100, 0));

        panelTop.add(ipAddressTextField);
        panelTop.add(portTextField);
        panelTop.add(onTopCheckBox);
        panelTop.add(loginTextField);
        panelTop.add(passwordField);
        panelTop.add(loginButton);
        panelBottom.add(logoutButton, BorderLayout.WEST);
        panelBottom.add(messageTextField, BorderLayout.CENTER);
        panelBottom.add(sendButton, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
        add(userListScrollPane, BorderLayout.EAST);

        onTopCheckBox.addActionListener(this);
        sendButton.addActionListener(this);
        messageTextField.addActionListener(this);
        loginButton.addActionListener(this);
        logoutButton.addActionListener(this);

        setUIConnection(false, null);
        setVisible(true);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source.equals(onTopCheckBox)) {
            setAlwaysOnTop(onTopCheckBox.isSelected());
        } else if (source.equals(sendButton) || source.equals(messageTextField)) {
            sendMessage();
        } else if (source.equals(loginButton)) {
            connect();
        } else if (source.equals(logoutButton)) {
            disconnect();
        } else {
            throw new IllegalStateException("Unexpected event");
        }
    }

    private void connect() {
        try {
            Socket socket = new Socket(ipAddressTextField.getText(), Integer.parseInt(portTextField.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void disconnect() {
        socketThread.close();
    }

    private void sendMessage() {
        String text = messageTextField.getText();
        if (text.equals("")) return;
        messageTextField.setText("");
        messageTextField.requestFocus();
        if (usersList.isSelectionEmpty()) {
            socketThread.sendMessage(NChMP.getClientBroadcast(text));
        } else {
            List<String> nicknames = usersList.getSelectedValuesList();
            StringBuilder sb = new StringBuilder();
            nicknames.forEach(user -> sb.append(user).append(NChMP.DELIMITER));
            socketThread.sendMessage(NChMP.getClientPrivate(text, sb.toString()));
        }
    }

    private void setUIConnection(boolean flag, String login) {
        panelTop.setVisible(!flag);
        panelBottom.setVisible(flag);
        setTitle(TITLE + (login != null ? " logged in as: " + login : ""));
        if (!flag) usersList.setListData(new String[0]);
    }

    private void putLog(String message) {
        if (message.equals("")) return;
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
        writeMessageToFile(message);
    }

    private void writeMessageToFile(String text) {
        try (FileWriter out = new FileWriter("log_chat.txt", true)) {
            out.write(text + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    public void showException(Thread thread, Throwable throwable) {
        String message = throwable.getStackTrace().length == 0 ? "Empty stack trace" :
                String.format("Exception in thread %s %s: %s%n%s", thread.getName(),
                        throwable.getClass().getCanonicalName(), throwable.getMessage(),
                        throwable.getStackTrace()[0]);
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
//        showException(thread, throwable);
    }

    @Override
    public void onSockedStart(SocketThread thread, Socket socket) {
        putLog("Start");
    }

    @Override
    public void onSockedStop(SocketThread thread) {
//        putLog("Stop");
        setUIConnection(false, null);
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
//        putLog("Ready");
        String login = loginTextField.getText();
        String password = new String(passwordField.getPassword());
        thread.sendMessage(NChMP.getAuthRequest(login, password));
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String message) {
        handleMessage(message);
//
    }

    private void handleMessage(String message) {
        System.out.println(message);
        String[] strArray = message.split(NChMP.DELIMITER);
        String messageType = strArray[0];
        switch (messageType) {
            case NChMP.AUTH_ACCEPT -> setUIConnection(true, strArray[1]);
            case NChMP.AUTH_DENY -> putLog(message);
            case NChMP.MESSAGE_FORMAT_ERROR -> {
                putLog(message);
                socketThread.close();
            }
            case NChMP.USER_LIST -> {
                String users = message.substring(NChMP.USER_LIST.length() + NChMP.DELIMITER.length());
                String[] usersArray = users.split(NChMP.DELIMITER);
                Arrays.sort(usersArray);
                usersList.setListData(usersArray);
            }
            case NChMP.MESSAGE_BROADCAST -> putLog(String.format("%s: %s: %s",
                    DATE_FORMAT.format(Long.parseLong(strArray[1])),
                    strArray[2], strArray[3]));
            case NChMP.MESSAGE_PRIVATE -> putLog(String.format("%s: %s private: %s",
                    DATE_FORMAT.format(Long.parseLong(strArray[1])),
                    strArray[2], strArray[3]));
            default -> throw new RuntimeException("Unknown message type: " + messageType);
        }
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        showException(thread, throwable);
    }
}