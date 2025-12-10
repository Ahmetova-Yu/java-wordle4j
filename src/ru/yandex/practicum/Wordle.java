package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Wordle {
    public static void main(String[] args) throws IOException {

        PrintWriter log = new PrintWriter(new FileWriter("wordle.log", StandardCharsets.UTF_8));
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dict = loader.loadDictionary();

        WordleGame game = new WordleGame(dict, log);

    }
}
