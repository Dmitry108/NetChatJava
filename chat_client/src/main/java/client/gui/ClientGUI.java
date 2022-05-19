package client.gui;

import client.core.ChatClient;
import client.core.ChatClientImpl;
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

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatClientView {

    private ChatClient chatClient = new ChatClientImpl(this);

    public static final int WIDTH = 600;
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

        setUIConnection(false);
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
            chatClient.connect(ipAddressTextField.getText(), portTextField.getText());
        } else if (source.equals(logoutButton)) {
            chatClient.disconnect();
        } else {
            throw new IllegalStateException("Unexpected event");
        }
    }

    private void sendMessage() {
        String text = messageTextField.getText();
        if (text.equals("")) return;
        messageTextField.setText("");
        messageTextField.requestFocus();
        chatClient.sendMessage(loginTextField.getText(), text);
    }

    private void setUIConnection(boolean flag) {
        panelTop.setVisible(!flag);
        panelBottom.setVisible(flag);
    }

    @Override
    public void putLog(String message) {
        if (message.equals("")) return;
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    @Override
    public void showException(String message) {
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
        chatClient.onException(thread, throwable);
    }
    @Override
    public void onConnect() {
        setUIConnection(true);
        String login = loginTextField.getText();
        String password = new String(passwordField.getPassword());
        chatClient.tryToLogin(login, password);
    }
    @Override
    public void onDisconnect() {
        setUIConnection(false);
    }
}