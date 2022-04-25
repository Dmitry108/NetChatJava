package server.gui;

import server.core.ChatServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {
    private final ChatServer server = new ChatServer();

    public static final int POS_X = 200;
    public static final int POS_Y = 200;
    public static final int WIDTH = 200;
    public static final int HEIGHT = 100;

    private static final JButton startButton = new JButton("Start");
    private static final JButton stopButton = new JButton("Stop");

    public ServerGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setResizable(false);
        setTitle("Chat Server");
        setLayout(new GridLayout(1, 2));
        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        add(startButton);
        add(stopButton);
        setVisible(true);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerGUI());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source.equals(startButton)) {
            server.start(222);
        } else if (source.equals(stopButton)) {
            server.stop();
        } else {
            throw new IllegalStateException("Unexpected event");
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String message = String.format("Exception in thread %s %s: %s%n%s", thread.getName(),
                throwable.getClass().getCanonicalName(), throwable.getMessage(), throwable.getStackTrace()[0]);
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}