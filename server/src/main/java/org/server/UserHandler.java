package org.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class UserHandler {

    private final Object lock = new Object();


    public boolean isUserNameAvailable(String username) {
        synchronized (lock) {

            Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream("users.txt"));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(username)) {
                    scanner.close();
                    return false;
                }
            }
            scanner.close();
            return true;
        }
    }

    public void register(String username, String password) throws URISyntaxException, IOException {
        synchronized (lock) {
 var file = new File(getClass().getClassLoader().getResource("users.txt").toURI());
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(username + " " + password);
            fileWriter.close();
        }
    }

    public boolean login(String username, String password) {
        synchronized (lock) {

            Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream("/users.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(username + " " + password)) {
                    scanner.close();
                    return true;
                }
            }
            scanner.close();
            return false;
        }
    }
}
