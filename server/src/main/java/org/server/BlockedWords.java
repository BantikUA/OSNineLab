package org.server;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class BlockedWords {

    private ArrayList<Moderator> moderators;
    private final Object lock = new Object();

private class Moderator {
    private final String word;

    private Moderator(String word) {
        this.word = word;
    }

    public boolean chek(String word) {
        return this.word.equals(word);
    }
}

public BlockedWords() throws FileNotFoundException {
    moderators = new ArrayList<>();
    load();
}

    private void load() throws FileNotFoundException {
        synchronized (lock) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bannedWords.txt");
            if (inputStream == null) {
                throw new FileNotFoundException("Файл не знайдено: /blockedWords.txt");
            }
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                moderators.add(new Moderator(line));
            }
            scanner.close();
        }
    }

    public int isBlocked(String str) {
        synchronized (lock) {
        int count = 0;
            String[] words = str.split("[ _.,!?:;]");
        for (Moderator moderator : moderators) {
            for (String word : words) {
                if (moderator.chek(word.toLowerCase(Locale.ROOT))) {
                    count++;
                }
            }
        }
        return count;
        }
}

}

