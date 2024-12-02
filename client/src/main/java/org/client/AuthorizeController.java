package org.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class AuthorizeController {
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

    private boolean isSignUp = true;
    private Socket socket;

    public AuthorizeController() {
        try {
            // 1. Отримання IP-адреси сервера через UDP
            String serverIp = getServerIp();
            if (serverIp == null) {
                throw new RuntimeException("Не вдалося отримати IP-адресу сервера.");
            }
            System.out.println("Отримано IP-адресу сервера: " + serverIp);

            // 2. Підключення до сервера через TCP
            int serverPort = 8080;
            socket = new Socket(serverIp, serverPort);

        } catch (IOException ex) {
            System.out.println("Помилка підключення до сервера: " + ex.getMessage());
        }
    }

    private String getServerIp() {
        try (DatagramSocket datagramSocket = new DatagramSocket(9876)) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Очікуємо IP-адресу сервера...");
            datagramSocket.receive(packet);

            String receivedData = new String(packet.getData(), 0, packet.getLength());
            if (receivedData.startsWith("SERVER_IP:")) {
                return receivedData.split(":")[1].trim();
            }
        } catch (IOException e) {
            System.out.println("Помилка при отриманні IP-адреси: " + e.getMessage());
        }
        return null;
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

            if (Objects.equals(response, "0")) {
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
        stage.show();

        Stage currentStage = (Stage) authorizeButton.getScene().getWindow();
        currentStage.close();
    }
}