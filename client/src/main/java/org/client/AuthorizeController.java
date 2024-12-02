package org.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

public class AuthorizeController {
    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private Socket socket;
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

    public AuthorizeController() {
        try {
            // Використання jmdns для пошуку сервісу
            JmDNS jmDNS = JmDNS.create(InetAddress.getLocalHost());
            ServiceInfo[] services = jmDNS.list(SERVICE_TYPE);

            if (services.length == 0) {
                throw new RuntimeException("Сервер не знайдено через jmdns!");
            }

            // Підключаємося до першого знайденого сервісу
            ServiceInfo serviceInfo = services[0];
            String serverIp = serviceInfo.getHostAddresses()[0];
            int serverPort = serviceInfo.getPort();

            System.out.println("Підключення до сервера: " + serverIp + ":" + serverPort);
            socket = new Socket(serverIp, serverPort);

        } catch (IOException ex) {
            throw new RuntimeException("Помилка підключення до сервера через jmdns: " + ex.getMessage());
        }
    }
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
        try {
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

            if (Objects.equals(response, "0\n")) {
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
    private void onAuthorizeButtonClick() throws IOException {
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

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("chat.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        ChatController chatController = fxmlLoader.getController();
        Stage stage = new Stage();

        chatController.SetSocket(socket);

        stage.setTitle("Forum");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        Stage currentStage = (Stage) authorizeButton.getScene().getWindow();
        currentStage.close();
    }
}