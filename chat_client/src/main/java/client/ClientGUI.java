package client;

import network.SocketThread;
import network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;

    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField ipAddressTextField = new JTextField("127.0.0.1");
    private final JTextField portTextField = new JTextField("222");
    private final JCheckBox onTopCheckBox = new JCheckBox("Always on top");
    private final JTextField loginTextField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Start");
    private final JTextArea logTextArea = new JTextArea();
    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton logoutButton = new JButton("Logout");
    private final JTextField messageTextField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final JList<String> usersList = new JList<>();

    private SocketThread socketThread;
    private String login;

    public ClientGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle("Chat");

        logTextArea.setEditable(false);
        logTextArea.setLineWrap(true);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        usersList.setPreferredSize(new Dimension(100, 0));
        JScrollPane userListScrollPane = new JScrollPane(usersList);

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

        isConnection(false);
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
            login = loginTextField.getText();
            socketThread = new SocketThread(this, login, socket);
            isConnection(true);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void disconnect() {
        isConnection(false);
    }

    private void sendMessage() {
        String text = messageTextField.getText();
        if (text.equals("")) return;
        messageTextField.setText("");
        messageTextField.requestFocus();
        text = String.format("%s: %s", login, text);
        socketThread.sendMessage(text);
        writeMessageToFile(text);
    }

    private void isConnection(boolean flag) {
        panelTop.setVisible(!flag);
        panelBottom.setVisible(flag);
    }

    private void putLog(String message) {
        if (message.equals("")) return;
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void writeMessageToFile(String text) {
        try (FileWriter out = new FileWriter("log_chat.txt", true)) {
            out.write(text + "\n");
            out.flush();
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
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
        showException(thread, throwable);
    }

    @Override
    public void onSockedStart(SocketThread thread, Socket socket) {
        putLog("Start");
    }

    @Override
    public void onSockedStop(SocketThread thread) {
        putLog("Stop");
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Ready");
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String message) {
        putLog(message);
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        showException(thread, throwable);
    }
}