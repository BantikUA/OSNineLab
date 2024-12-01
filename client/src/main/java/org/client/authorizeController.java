package org.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class authorizeController {
    final private String serverIp = "localhost";
    final private int serverPort = 8080;

    private boolean isSignUp = true;

    @FXML
    private Button signUpButton;
    @FXML
    private Button logInButton;
    @FXML
    private Button authorizeButton;
    @FXML
    private Label errorMsg;
    @FXML
    private Label authorizeText;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void onSignUpButtonClick() {
        isSignUp = true;
        signUpButton.setVisible(false);
        signUpButton.setManaged(false);
        logInButton.setVisible(true);
        logInButton.setManaged(true);
        loginField.setText("");
        passwordField.setText("");
        authorizeText.setText("Sign up to Client");
        authorizeButton.setText("Sign up");
        clearError();
    }

    @FXML
    private void onLogInButtonClick() {
        isSignUp = false;
        logInButton.setVisible(false);
        logInButton.setManaged(false);
        signUpButton.setVisible(true);
        signUpButton.setManaged(true);
        loginField.setText("");
        passwordField.setText("");
        authorizeText.setText("Log in to Client");
        authorizeButton.setText("Log in");
        clearError();
    }

    private void writeError(String error) {
        errorMsg.setText(error);
        errorMsg.setVisible(true);
        errorMsg.setManaged(true);
    }

    private void clearError() {
        errorMsg.setVisible(false);
        errorMsg.setManaged(false);
        errorMsg.setText("");
    }

    private boolean validateFields() {
        if (loginField.getText().isEmpty() && passwordField.getText().isEmpty()) {
            writeError("Please fill in all fields");
            return false;
        } else if (loginField.getText().isEmpty()) {
            writeError("Please fill in login field");
            return false;
        } else if (passwordField.getText().isEmpty()) {
            writeError("Please fill in password field");
            return false;
        }

        clearError();
        return true;
    }

    private boolean sentToServer(String login, String password) {
        try (Socket socket = new Socket(serverIp, serverPort)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            if (isSignUp) {
                writer.println("REG_USR://:" + login + ":" + password);
            } else {
                writer.println("LOG_USR://:" + login + ":" + password);
            }

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String response = reader.readLine();

            System.out.println(response);

            if (!Boolean.parseBoolean(response)) {
                return false;
            }

        } catch (UnknownHostException ex) {
            throw new RuntimeException("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            throw new RuntimeException("I/O error: " + ex.getMessage());
        }
        return true;
    }

    @FXML
    private void onAuthorizeButtonClick() {
        if (!validateFields()) {
            return;
        }

        try {
            if (!sentToServer(loginField.getText(), passwordField.getText())) {
                writeError("That login is already taken");
                return;
            }
        } catch (Exception e) {
            writeError(e.getMessage());
            return;
        }
    }
}