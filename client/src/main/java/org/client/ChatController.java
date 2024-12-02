package org.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.*;
import java.net.*;

public class ChatController {
    private PrintWriter out;
    private BufferedReader in;

    public void SetSocket(Socket socket)
    {
        try {
            // Ініціалізація вхідного та вихідного потоків
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startMessageListener();
        } catch (IOException e) {
            throw new RuntimeException("Error initializing socket streams: " + e.getMessage());
        }
    }

    @FXML
    private TextArea messageArea;

    @FXML
    private TextField inputField;

    @FXML
    private void handleSendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            sendToServer(message);
            inputField.clear(); // Очищення поля вводу після відправки
        }
    }

    private void sendToServer(String message)
    {
        if (out != null) {
            out.println("MSG_USR://:" + message);
        } else {
            throw new RuntimeException("Output stream is not initialized.");
        }

    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false; // Якщо рядок порожній або null
        }
        try {
            Integer.parseInt(str); // Спроба перетворити рядок у ціле число
            return true; // Якщо успішно, повертаємо true
        } catch (NumberFormatException e) {
            return false; // Якщо виникає помилка, повертаємо false
        }
    }

    private void startMessageListener() {

        Thread messageListener = new Thread(() -> {

                while (true) { // Нескінченний цикл для зчитування повідомлень
                    try {
                        String serverMessage = in.readLine(); // Читання повідомлення від сервера
                        if (serverMessage != null) {
                            String finalMessage = serverMessage;
                            if (isInteger(finalMessage)) {
                                throw new IOException(finalMessage);
                            }
                            javafx.application.Platform.runLater(() ->
                                    messageArea.appendText(finalMessage + "\n")
                            );
                        } else {
                            break; // Вихід з циклу, якщо з'єднання закрито (readLine() повертає null)
                        }
                    } catch (IOException e) {
                        String errorMessage = e.getMessage(); // Отримуємо повідомлення з винятка
                        javafx.application.Platform.runLater(() -> {
                            // Відображаємо повідомлення у вікні Alert
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Server Error");
                            alert.setContentText("Перевищена к-ть заборонених слів. К-ть заборонених слів: " + errorMessage);
                            alert.showAndWait();
                        });

                    }
                }
        });

        // Запускаємо потік у фоновому режимі
        messageListener.setDaemon(true); // Потік завершиться разом із додатком
        messageListener.start();
    }

    @FXML
    private void initialize() {
        // Прив'язуємо подію Enter до текстового поля
        inputField.setOnAction(event -> handleSendMessage());
    }

}