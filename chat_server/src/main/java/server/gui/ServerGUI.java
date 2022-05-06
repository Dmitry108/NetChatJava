package server.gui;

import server.core.ChatServer;
import server.core.ChatServerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatServerListener {
    private final ChatServer server = new ChatServer(this);

    public static final int POS_X = 200;
    public static final int POS_Y = 200;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 300;

    private final JButton startButton = new JButton("Start");
    private final JButton stopButton = new JButton("Stop");
    private final JPanel panelTop = new JPanel(new GridLayout(1,2));
    private final JTextArea log = new JTextArea();

    public ServerGUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setResizable(false);
        setTitle("Chat Server");
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(log);
        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        panelTop.add(startButton);
        panelTop.add(stopButton);
        add(panelTop, BorderLayout.NORTH);
        add(logScroll, BorderLayout.CENTER);
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
        throwable.printStackTrace();
//        String message = String.format("Exception in thread %s %s: %s%n%s", thread.getName(),
//                throwable.getClass().getCanonicalName(), throwable.getMessage(), throwable.getStackTrace()[0]);
//        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onChatServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            log.append(message + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }
}