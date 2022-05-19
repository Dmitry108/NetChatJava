package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegistrationGUI extends JFrame implements ActionListener {
    private static RegistrationGUI instance;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;
    private static final String TITLE = "Registration";

    private Client client;
    private final JPanel panel = new JPanel(new GridLayout(4, 1));
    private final JLabel loginLabel = new JLabel("Login");
    private final JTextField loginTextField = new JTextField();
    private final JLabel nicknameLabel = new JLabel("Nickname");
    private final JTextField nickTextField = new JTextField();
    private final JLabel passwordLabel = new JLabel("Password");
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel errorLabel = new JLabel();
    private final JButton registerButton = new JButton("Register");

    private RegistrationGUI() {
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);

        panel.add(loginLabel);
        panel.add(loginTextField);
        panel.add(nicknameLabel);
        panel.add(nickTextField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        add(errorLabel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add(registerButton, BorderLayout.SOUTH);

        registerButton.addActionListener(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
    }

    public static RegistrationGUI getInstance() {
        if (instance == null) {
            instance = new RegistrationGUI();
        }
        return instance;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source.equals(registerButton)) {
            doRegistration();
        } else {
            throw new IllegalStateException("Unexpected event");
        }
    }

    private void doRegistration() {
        errorLabel.setText("");
        String login = loginTextField.getText();
        String nickname = nickTextField.getText();
        String password = new String(passwordField.getPassword());
        if (login.equals("")) {
            errorLabel.setText("Login is not filled");
            loginTextField.requestFocus();
            return;
        }
        if (nickname.equals("")) {
            errorLabel.setText("Nickname is not filled");
            nickTextField.requestFocus();
            return;
        }
        if (password.equals("")) {
            errorLabel.setText("Password");
            loginTextField.requestFocus();
            return;
        }
        client.register(login, nickname, password);
        setVisible(false);
    }
}