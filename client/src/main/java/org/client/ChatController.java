package org.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.*;
import java.net.*;

public class ChatController {
    final private String ServerIp = "localhost";
    final private int ServerPort = 8080;

    private Socket globalSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void SetSocket(Socket socket)
    {
        globalSocket = socket;

        try {
            // Ініціалізація вхідного та вихідного потоків
            in = new BufferedReader(new InputStreamReader(globalSocket.getInputStream()));
            out = new PrintWriter(globalSocket.getOutputStream(), true);

            // Запуск потоку для отримання повідомлень
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
            //messageArea.appendText("Ви: " + message + "\n");
            inputField.clear(); // Очищення поля вводу після відправки
        }
    }

    private void sendToServer(String message)
    {
//        try
//        {
//            OutputStream output = globalSocket.getOutputStream();
//            PrintWriter writer = new PrintWriter(output, true);
//
//            writer.println("MSG_USR://:"+message);
//        }
//        catch (UnknownHostException ex) {
//            throw new RuntimeException("Server not found: " + ex.getMessage());
//        } catch (IOException ex) {
//            throw new RuntimeException("I/O error: " + ex.getMessage());
//        }

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
//        Thread messageListener = new Thread(() -> {
//            try {
//                String serverMessage;
//                // Постійно слухаємо повідомлення від сервера
//                while ((serverMessage = in.readLine()) != null) {
//                    String finalMessage = serverMessage;
//                    // Оновлюємо текстове поле у потоці JavaFX
//                    javafx.application.Platform.runLater(() -> messageArea.appendText("Сервер: " + finalMessage + "\n"));
//                }
//            } catch (IOException e) {
//                System.err.println("Error reading messages: " + e.getMessage());
//            }
//        });

        Thread messageListener = new Thread(() -> {
            try {
                while (true) { // Нескінченний цикл для зчитування повідомлень
                    String serverMessage = in.readLine(); // Читання повідомлення від сервера
                    if (serverMessage != null) {
                        String finalMessage = serverMessage;
                        if(isInteger(finalMessage))
                        {
                            throw new IOException(finalMessage);
                        }
                        javafx.application.Platform.runLater(() ->
                                messageArea.appendText(finalMessage + "\n")
                        );
                    } else {
                        break; // Вихід з циклу, якщо з'єднання закрито (readLine() повертає null)
                    }
                }
            } catch (IOException e) {
                String errorMessage = e.getMessage(); // Отримуємо повідомлення з винятка
                javafx.application.Platform.runLater(() -> {
                    // Відображаємо повідомлення у вікні Alert
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Server Error");
                    alert.setContentText("Error: " + errorMessage);
                    alert.showAndWait();
                });
                //System.err.println("Error reading messages: " + e.getMessage());
            }
        });

        // Запускаємо потік у фоновому режимі
        messageListener.setDaemon(true); // Потік завершиться разом із додатком
        messageListener.start();
    }

//    @FXML
//    private Label welcomeText;
//
//    @FXML
//    protected void onHelloButtonClick() {
//        welcomeText.setText("Welcome to JavaFX Application!");
//    }
}